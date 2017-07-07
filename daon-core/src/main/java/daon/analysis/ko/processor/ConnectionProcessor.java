package daon.analysis.ko.processor;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import daon.analysis.ko.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ConnectionProcessor {

    private Logger logger = LoggerFactory.getLogger(ConnectionProcessor.class);


    private ModelInfo modelInfo;

    public static ConnectionProcessor create(ModelInfo modelInfo) {

        return new ConnectionProcessor(modelInfo);
    }

    private ConnectionProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    /**
     * 최종 result 구성
     * @param prevResultInfo
     * @param resultInfo
     * @param nextResultInfo
     */
    public void process(ResultInfo prevResultInfo, ResultInfo resultInfo, ResultInfo nextResultInfo) {

        ConnectionFinder finder = new ConnectionFinder(modelInfo);

        FilterSet beforeBestFilterSet = resultInfo.getBestFilterSet();
        List<FilterSet> filterSets = resultInfo.getFilteredSet();

        TreeSet<BestSet> bestSets = new TreeSet<>(scoreComparator);


        if(beforeBestFilterSet == null) {

            for (FilterSet curSet : filterSets) {

                if (nextResultInfo != null) {

                    for (FilterSet next : nextResultInfo.getFilteredSet()) {
                        BestSet bestSet = new BestSet(modelInfo, finder);
                        bestSet.setCur(curSet);
                        bestSet.setNext(next);

                        bestSet.calculateScore();
                        bestSets.add(bestSet);
                    }


                } else {
                    BestSet bestSet = new BestSet(modelInfo, finder);
                    bestSet.setCur(curSet);

                    bestSet.calculateScore();
                    bestSets.add(bestSet);

                }
            }
        }else{
            if (nextResultInfo != null) {

                for (FilterSet next : nextResultInfo.getFilteredSet()) {
                    BestSet bestSet = new BestSet(modelInfo, finder);
                    bestSet.setCur(beforeBestFilterSet);
                    bestSet.setNext(next);

                    bestSet.calculateScore();
                    bestSets.add(bestSet);
                }


            } else {
                BestSet bestSet = new BestSet(modelInfo, finder);
                bestSet.setCur(beforeBestFilterSet);

                bestSet.calculateScore();
                bestSets.add(bestSet);

            }
        }

        BestSet bestSet = bestSets.first();

        //후보셋 정보 보기
        if(logger.isDebugEnabled()) {
            logger.debug("##############################");
            bestSets.stream().limit(10).forEach(r -> logger.debug("result : {}", r));
        }

        FilterSet bestFilterSet = bestSet.getCur();
        FilterSet nextBestFilterSet = bestSet.getNext();

        resultInfo.setBestFilterSet(bestFilterSet);

        if(nextResultInfo != null) {
            nextResultInfo.setBestFilterSet(nextBestFilterSet);
        }
    }



    static final Comparator<BestSet> scoreComparator = (BestSet left, BestSet right) -> {
        return left.getScore() > right.getScore() ? -1 : 1;
    };



}
