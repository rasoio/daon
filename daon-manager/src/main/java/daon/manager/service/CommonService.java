package daon.manager.service;

import daon.core.config.POSTag;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

public abstract class CommonService {

    public void addCondition(BoolQueryBuilder mainQueryBuilder, String fieldName, String condition, String value){

        if(StringUtils.isNoneBlank(value)){
            if("equals".equals(condition)){
                mainQueryBuilder.must(matchQuery(fieldName, value));
            }else {
                value = toConditionValue(condition, value);
                mainQueryBuilder.must(wildcardQuery(fieldName, value));
            }
        }
    }

    public void addConditionNested(BoolQueryBuilder mainQueryBuilder, String nestedName, String fieldName, String condition, String value){

        if(StringUtils.isNoneBlank(value)){
            if("equals".equals(condition)){
                mainQueryBuilder.must(nestedQuery(nestedName, matchQuery(fieldName, value), ScoreMode.None));
            }else {
                value = toConditionValue(condition, value);
                mainQueryBuilder.must(nestedQuery(nestedName, wildcardQuery(fieldName, value), ScoreMode.None));
            }
        }
    }

    private String toConditionValue(String condition, String value) {
        if("prefix".equals(condition)){
            value = value + "*";
        }else if("suffix".equals(condition)){
            value = "*" + value;
        }else{
            value = "*" + value + "*";
        }
        return value;
    }
}
