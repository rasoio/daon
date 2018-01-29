package daon.core.handler;

import daon.core.config.POSTag;
import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;

import java.util.ArrayList;
import java.util.List;

public class EchoHandler implements EojeolInfoHandler {

    private List<EojeolInfo> list = new ArrayList<>();

    @Override
    public void handle(EojeolInfo eojeolInfo) {
        list.add(eojeolInfo);
    }

    @Override
    public List<EojeolInfo> getList() {
        return list;
    }

}
