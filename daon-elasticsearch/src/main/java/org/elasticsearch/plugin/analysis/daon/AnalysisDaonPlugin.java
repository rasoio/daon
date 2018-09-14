package org.elasticsearch.plugin.analysis.daon;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.store.ByteArrayDataInput;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.DaonModelAction;
import org.elasticsearch.action.TransportDaonModelAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestDaonModel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.SearchScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;


public class AnalysisDaonPlugin extends Plugin implements AnalysisPlugin, ActionPlugin, ScriptPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisDaonPlugin.class);

    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisProvider<TokenFilterFactory>> extra = new HashMap<>();
        extra.put("daon_filter", DaonFilterFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisProvider<TokenizerFactory>> getTokenizers() {
        return singletonMap("daon_tokenizer", DaonTokenizerFactory::new);
    }

    @Override
    public Map<String, AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return singletonMap("daon_analyzer", DaonAnalyzerProvider::new);
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Arrays.asList(new ActionHandler<>(DaonModelAction.INSTANCE, TransportDaonModelAction.class));
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        return singletonList(new RestDaonModel(settings, restController));
    }

    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
        return new VectorScoreScriptEngine();
    }

    /** An example {@link ScriptEngine} that uses Lucene segment details to implement pure document frequency scoring. */
    // tag::expert_engine
    private static class VectorScoreScriptEngine implements ScriptEngine {


        @Override
        public String getType() {
            return "vector_score_scripts";
        }

        @Override
        public <T> T compile(String scriptName, String scriptSource, ScriptContext<T> context, Map<String, String> params) {
            if (context.equals(ScoreScript.CONTEXT) == false) {
                throw new IllegalArgumentException(getType() + " scripts cannot be used for context [" + context.name + "]");
            }
            // we use the script "source" as the script identifier
            if ("cosine_similarity".equals(scriptSource)) {
                ScoreScript.Factory factory = (p, lookup) -> new ScoreScript.LeafFactory() {

                    private static final int DOUBLE_SIZE = 8;
                    final double[] inputVector;
                    final double queryVectorNorm;
                    final int size;
                    final String field;
                    {
                        if (p.containsKey("field") == false) {
                            throw new IllegalArgumentException("Missing parameter [field]");
                        }
                        field = p.get("field").toString();

                        final Object vector = p.get("vector");
                        if(vector != null) {
                            @SuppressWarnings("unchecked")
                            final ArrayList<Double> tmp = (ArrayList<Double>) vector;
                            inputVector = new double[tmp.size()];
                            for (int i = 0; i < inputVector.length; i++) {
                                inputVector[i] = tmp.get(i);
                            }
                        } else {
                            final Object encodedVector = p.get("encoded_vector");
                            if(encodedVector == null) {
                                throw new IllegalArgumentException("Must have at 'vector' or 'encoded_vector' as a parameter");
                            }
                            inputVector = Util.convertBase64ToArray((String) encodedVector);
                        }

                        size = inputVector.length;

                        // calc queryVectorNorm
                        double queryVectorNorm = 0.0;
                        // compute query inputVector norm once
                        for (double v : inputVector) {
                            queryVectorNorm += (v * v);
                        }

                        this.queryVectorNorm =  Math.sqrt(queryVectorNorm);

                        logger.info("field : {}, inputVector : {}, queryVectorNorm : {}", field, inputVector, this.queryVectorNorm);
                    }

                    @Override
                    public ScoreScript newInstance(LeafReaderContext context) throws IOException {

                        BinaryDocValues binaryDocValues = context.reader().getBinaryDocValues(field);

                        if (binaryDocValues == null) {
                            // the field and/or term don't exist in this segment, so always return 0
                            return new ScoreScript(p, lookup, context) {
                                @Override
                                public double execute() {
                                    return 0.0d;
                                }
                            };
                        }
                        return new ScoreScript(p, lookup, context) {
                            int currentDocid = -1;
                            @Override
                            public void setDocument(int docid) {
                                // advance has undefined behavior calling with a docid <= its current docid
                                if (binaryDocValues.docID() < docid) {
                                    try {
                                        binaryDocValues.advance(docid);
                                    } catch (IOException e) {
                                        throw new UncheckedIOException(e);
                                    }
                                }
                                currentDocid = docid;
                            }
                            @Override
                            public double execute() {
                                double score = 0.0d;
                                try {
                                    if(binaryDocValues.advanceExact(currentDocid)){
                                        BytesRef bytesRef = binaryDocValues.binaryValue();
                                        byte[] bytes = bytesRef.bytes;

                                        final ByteArrayDataInput input = new ByteArrayDataInput(bytes);
                                        input.readVInt(); // returns the number of values which should be 1, MUST appear hear since it affect the next calls
                                        final int len = input.readVInt(); // returns the number of bytes to read
                                        if(len != size * DOUBLE_SIZE) {
                                            return 0.0;
                                        }
                                        final int position = input.getPosition();
                                        final DoubleBuffer doubleBuffer = ByteBuffer.wrap(bytes, position, len).asDoubleBuffer();

                                        final double[] docVector = new double[size];
                                        doubleBuffer.get(docVector);

                                        double docVectorNorm = 0.0f;
                                        for (int i = 0; i < size; i++) {
                                            // doc inputVector norm
                                            docVectorNorm += (docVector[i] * docVector[i]);
                                            // dot product
                                            score += docVector[i] * inputVector[i];
                                        }

                                        docVectorNorm = Math.sqrt(docVectorNorm);

                                        // cosine similarity score
                                        if (docVectorNorm == 0 || queryVectorNorm == 0){
                                            return 0f;
                                        } else {
                                            return score / (docVectorNorm * queryVectorNorm);
                                        }
                                    }
                                } catch (IOException e) {
                                    logger.error("vector similarity score error", e);
                                }

                                return score;
                            }
                        };
                    }

                    @Override
                    public boolean needs_score() {
                        return false;
                    }
                };
                return context.factoryClazz.cast(factory);
            }
            throw new IllegalArgumentException("Unknown script name " + scriptSource);
        }

        @Override
        public void close() {
            // optionally close resources
        }
    }
    // end::expert_engine
}
