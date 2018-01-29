package daon.core.handler;

import daon.core.config.POSTag;
import daon.core.result.Keyword;
import daon.core.util.Utils;

import java.util.List;

public abstract class AbstractHandler implements EojeolInfoHandler {

    private long includeBit = -1;
    private long excludeBit = -1;

    public void setInclude(List<String> include){
        includeBit = Utils.makeTagBit(include);
    }

    public void setExclude(List<String> exclude){
        excludeBit = Utils.makeTagBit(exclude);
    }

    public void setIncludeBit(long includeBit) {
        this.includeBit = includeBit;
    }

    public void setExcludeBit(long excludeBit) {
        this.excludeBit = excludeBit;
    }

    protected boolean isValid(Keyword keyword) {

        if(includeBit > -1) {
            if (!Utils.containsTag(includeBit, keyword.getTag())) {
                return false;
            }
        }else if(excludeBit > -1){
            if (Utils.containsTag(excludeBit, keyword.getTag())) {
                return false;
            }
        }

        return true;
    }

}
