package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.ModelInfo;
import daon.manager.model.data.AliasIndices;
import daon.manager.model.data.AnalyzedEojeol;
import daon.manager.model.data.Index;
import daon.manager.model.data.Term;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.AliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class AliasService {

	@Autowired
	private TransportClient client;

	public Map<String, List<Index>> alias() throws IOException {

        Map<String, AliasOrIndex> data = client.admin().cluster()
                .prepareState().execute()
                .actionGet().getState()
                .getMetaData().getAliasAndIndexLookup();

        //indices
        List<String> concreteIndex = data.entrySet().stream().filter(e -> e.getKey().contains("sentences"))
                .filter(e -> !e.getValue().isAlias())
                .map(Map.Entry::getKey).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        //alias 별 indices
        Map<String, List<Index>> aliases = data.entrySet().stream().filter(e -> e.getKey().contains("sentences"))
                .filter(e -> e.getValue().isAlias())
                .map(e -> {
                    String aliasName = e.getKey();
                    List<String> aliasIndices = e.getValue().getIndices().stream()
                            .map(i-> i.getIndex().getName()).collect(Collectors.toList());

                    List<Index> indices = concreteIndex.stream()
                            .map(index -> new Index(index, aliasIndices.contains(index))).collect(Collectors.toList());

                    return new AliasIndices(aliasName, indices);
                }).collect(Collectors.toMap(AliasIndices::getAlias, AliasIndices::getIndices));

		return aliases;
	}


	public boolean add(String[] indices, String alias){

        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = client.admin().indices().prepareAliases();
        IndicesAliasesResponse response = indicesAliasesRequestBuilder.addAlias(indices, alias).execute().actionGet();

        return response.isAcknowledged();
    }


    public boolean remove(String[] indices, String alias){

        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = client.admin().indices().prepareAliases();
        IndicesAliasesResponse response = indicesAliasesRequestBuilder.removeAlias(indices, alias).execute().actionGet();

        return response.isAcknowledged();
    }


    public boolean save(Map<String, List<Index>> data) {

        boolean isSuccess = true;

        for (Map.Entry<String, List<Index>> e : data.entrySet()) {
            String alias = e.getKey();

            List<Index> indices = e.getValue();

            String[] removeIndexNames = indices.stream().map(Index::getName).toArray(String[]::new);
            String[] addIndexNames = indices.stream().filter(Index::isExist).map(Index::getName).toArray(String[]::new);


            log.info("alias : {}, removeNames : {}, addNames : {}", alias, removeIndexNames, addIndexNames);
            boolean isRemove = remove(removeIndexNames, alias);
            boolean isAdd = add(addIndexNames, alias);

            if(!isRemove || !isAdd){
                isSuccess = false;
            }
        }

        return isSuccess;
    }
}