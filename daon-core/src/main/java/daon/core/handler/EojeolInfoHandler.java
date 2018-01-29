package daon.core.handler;

import daon.core.result.EojeolInfo;

import java.util.List;

public interface EojeolInfoHandler {

    public void handle(EojeolInfo eojeolInfo);

    public List<?> getList();
}
