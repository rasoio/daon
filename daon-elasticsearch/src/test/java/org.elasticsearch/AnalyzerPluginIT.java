package org.elasticsearch;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.TransportAnalyzeAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalysisRegistry;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugin.analysis.daon.AnalysisDaonPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.singletonList;

public class AnalyzerPluginIT extends ESTestCase {

    private IndexAnalyzers indexAnalyzers;
    private AnalysisRegistry registry;
    private Environment environment;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Path home = createTempDir();
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString()).build();

        Settings indexSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.SETTING_INDEX_UUID, UUIDs.randomBase64UUID())
                .put("index.analysis.analyzer.custom_analyzer.tokenizer", "daon_tokenizer")
                .put("index.analysis.analyzer.custom_analyzer.filter", "daon_filter").build();
        IndexSettings idxSettings = IndexSettingsModule.newIndexSettings("index", indexSettings);
        environment = new Environment(settings, home);

        AnalysisPlugin plugin = new AnalysisDaonPlugin();

        registry = new AnalysisModule(environment, singletonList(plugin)).getAnalysisRegistry();
        indexAnalyzers = registry.build(idxSettings);
    }

    /**
     * Test behavior when the named analysis component isn't defined on the index. In that case we should build with defaults.
     */
    public void testNoIndexAnalyzers() throws IOException {
        // Refer to an analyzer by its type so we get its default configuration
        AnalyzeRequest request = new AnalyzeRequest();
        request.text("the quick brown fox");
        request.analyzer("daon_analyzer");
        AnalyzeResponse analyze = TransportAnalyzeAction.analyze(request, "text", null, null, registry, environment);
        List<AnalyzeResponse.AnalyzeToken> tokens = analyze.getTokens();
        assertEquals(8, tokens.size());


        // We can refer to a pre-configured token filter by its name to get it
        request = new AnalyzeRequest();
        request.text("the qu1ck brown fox");
        request.tokenizer("daon_tokenizer");
        analyze = TransportAnalyzeAction.analyze(request, "text", null, randomBoolean() ? indexAnalyzers : null, registry, environment);
        tokens = analyze.getTokens();
        assertEquals(1, tokens.size());
        assertEquals("the qu1ck brown fox", tokens.get(0).getTerm());

        // We can refer to a token filter by its type to get its default configuration
        request = new AnalyzeRequest();
        request.text("the qu1ck brown fox");
        request.tokenizer("daon_tokenizer");
        request.addTokenFilter("daon_filter");
        analyze = TransportAnalyzeAction.analyze(request, "text", null, randomBoolean() ? indexAnalyzers : null, registry, environment);
        tokens = analyze.getTokens();

        assertEquals(10, tokens.size());
//        assertEquals("the", tokens.get(0).getTerm());
//        assertEquals("qu", tokens.get(1).getTerm());
//        assertEquals("1", tokens.get(2).getTerm());
//        assertEquals("ck", tokens.get(3).getTerm());
//        assertEquals("brown", tokens.get(4).getTerm());
//        assertEquals("fox", tokens.get(5).getTerm());
    }
}
