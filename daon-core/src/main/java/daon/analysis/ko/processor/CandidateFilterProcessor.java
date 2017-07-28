package daon.analysis.ko.processor;

import daon.analysis.ko.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mac on 2017. 5. 18..
 */
public class CandidateFilterProcessor {

    private Logger logger = LoggerFactory.getLogger(CandidateFilterProcessor.class);

    private ModelInfo modelInfo;

    public static CandidateFilterProcessor create(ModelInfo modelInfo) {

        return new CandidateFilterProcessor(modelInfo);
    }

    private CandidateFilterProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /**
     * 분석 후보중 상위 후보만 남기고 제거
     * @param resultInfo
     * @throws IOException
     */
    public void process(ResultInfo resultInfo) throws IOException {

        int length = resultInfo.getLength();

        ConnectionFinder finder = new ConnectionFinder(modelInfo);

//        Set<FilterSet> filterSets = new TreeSet<>(scoreComparator);

        List<FilterSet> filterSets = new ArrayList<>();

//        PriorityQueue

        CandidateTerms terms = resultInfo.getCandidateTerms(0);

        for (Term term : terms.getTerms()) {

            FilterSet filterSet = createFilterSet(finder, length);

            filterSet.add(term);

            filterSets.add(filterSet);
        }

        add(filterSets, resultInfo);

        List<FilterSet> results = filterSets.stream()
//                .filter(filterSet -> {return (filterSet.getLength() == filterSet.getMaxLength());})
                .sorted(scoreComparator).limit(5).collect(Collectors.toList());

        //후보셋 정보 보기
        if(logger.isDebugEnabled()) {
            logger.debug("##############################");
            results.forEach(r -> logger.debug("result : {}", r));
        }

        resultInfo.setFilteredSet(results);

    }

    private void add(List<FilterSet> filterSets, ResultInfo resultInfo){

        for(FilterSet filterSet : filterSets){
            while(true) {

                int idx = filterSet.getLength();

                CandidateTerms candidateTerms = resultInfo.getCandidateTerms(idx);

                if (candidateTerms == null) {
                    break;
                }

                filterSet.add(candidateTerms);
            }

        }

    }


    static final Comparator<FilterSet> scoreComparator = (FilterSet left, FilterSet right) -> {
        return left.getScore() > right.getScore() ? -1 : 1;
    };


    private FilterSet createFilterSet(ConnectionFinder finder, int length) {
        FilterSet filterSet = new FilterSet(modelInfo, finder, length);

        return filterSet;
    }

}
