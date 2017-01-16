package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.List;

import daon.analysis.ko.dict.config.Config.AlterRules;

public class MergeSet {

    private final AlterRules rule;

    private List<PrevInfo> prevList = new ArrayList<PrevInfo>();

    private List<NextInfo> nextList = new ArrayList<NextInfo>();

    //조합 시 제약 사항 정의 필요
    public MergeSet(AlterRules rule) {
        this.rule = rule;
    }

    public AlterRules getRule() {
        return rule;
    }

    public List<PrevInfo> getPrevList() {
        return prevList;
    }

    public void setPrevList(List<PrevInfo> prevList) {
        this.prevList = prevList;
    }

    public List<NextInfo> getNextList() {
        return nextList;
    }

    public void setNextList(List<NextInfo> nextList) {
        this.nextList = nextList;
    }

    public void addPrev(PrevInfo prev) {
        prevList.add(prev);
    }

    public void addNext(NextInfo next) {
        nextList.add(next);
    }

    public boolean isValid() {

        //prev, next 둘다 존재해야 조합 가능.
        if (prevList.size() > 0 && nextList.size() > 0) {
            return true;
        }

        return false;
    }

}
