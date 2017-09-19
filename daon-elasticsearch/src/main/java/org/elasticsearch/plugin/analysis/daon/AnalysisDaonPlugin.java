package org.elasticsearch.plugin.analysis.daon;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ModelReloadAction;
import org.elasticsearch.action.TransportModelReloadAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.index.analysis.DaonFilterFactory;
import org.elasticsearch.index.analysis.DaonTokenizerFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.DaonAnalyzerProvider;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestModelReloader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;


public class AnalysisDaonPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

//    @Override
//    public Map<String, AnalysisProvider<CharFilterFactory>> getCharFilters() {
//    }
//
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
        return unmodifiableList(singletonList(
                new ActionHandler<>(ModelReloadAction.INSTANCE, TransportModelReloadAction.class)
        ));
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        return singletonList(new RestModelReloader(settings, restController));
    }
}
