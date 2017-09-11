package daon.manager.service;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import daon.manager.model.data.Index;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class AliasService {

	@Autowired
	private TransportClient client;

	public Map<String, List<Index>> alias() throws IOException, ExecutionException, InterruptedException {

        Map<String, List<Index>> aliases = new HashMap<>();

        GetAliasesResponse response = client.admin().indices().getAliases(new GetAliasesRequest("_all")).get();

        ImmutableOpenMap<String, List<AliasMetaData>> data = response.getAliases();

        List<String> concreteIndices = StreamSupport.stream(data.keys().spliterator(), false)
                .filter(e -> e.value.contains("sentences")).map(e->e.value).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        for(ObjectObjectCursor<String, List<AliasMetaData>> d : data){

            String index = d.key;

            List<AliasMetaData> value = d.value;

            value.forEach(a -> {
                String alias = a.alias();
                List<Index> indices = aliases.get(alias);

                if(indices == null){
                    indices = new ArrayList<>();

                    for(String name : concreteIndices){
                        indices.add(new Index(name, false));
                    }
                }

                for(Index i : indices){
                    if(index.equals(i.getName())){
                        i.setExist(true);
                    }
                }

                aliases.put(alias, indices);
            });

        }

//        log.info("concreteIndices : {}, aliases : {}", concreteIndices, aliases);

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