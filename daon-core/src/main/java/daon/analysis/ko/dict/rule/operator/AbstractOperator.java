package daon.analysis.ko.dict.rule.operator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;

public abstract class AbstractOperator implements Operator {

    private Logger logger = LoggerFactory.getLogger(AbstractOperator.class);

    @Override
    public boolean execute(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo, List<KeywordRef> keywordRefs) {

        boolean isSuccess = false;

        KeywordRef ref = make(rule, prevInfo, nextInfo);

        if (ref != null) {
            keywordRefs.add(ref);
            isSuccess = true;
        }

        return isSuccess;
    }

    /**
     * 조합 수행
     *
     * @param rule     조합룰
     * @param prevInfo
     * @param nextInfo
     * @return
     */
    public abstract KeywordRef make(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo);


    /**
     * 조합 결과 KeywordRef 생성
     *
     * @param surface
     * @param desc
     * @param subKeywords
     * @return
     */
    public KeywordRef createKeywordRef(String surface, Keyword... subKeywords) {

        int len = subKeywords.length;
        long[] wordIds = new long[len];

        for (int i = 0; i < len; i++) {
            Keyword word = subKeywords[i];
            wordIds[i] = word.getSeq();
        }

        KeywordRef keyword = new KeywordRef(surface, subKeywords);

        return keyword;
    }


}
