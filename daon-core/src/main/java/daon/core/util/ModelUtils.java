package daon.core.util;

import daon.core.result.ModelInfo;
import daon.core.reader.ModelReader;

import java.io.InputStream;

public class ModelUtils {

    private static ModelInfo currentModelInfo;

    public static void init(){
        setModel(loadModel());
    }

    public static ModelInfo loadModel(){
        //초기 모델 파일 설정 옵션
        String filePath = System.getProperty("daon.model.file");
        String url = System.getProperty("daon.model.url");

        ModelReader modelReader = ModelReader.create();

        if(filePath != null){
            modelReader.filePath(filePath);
        }else if(url != null){
            modelReader.url(url);
        }

        return modelReader.load();
    }

    public static ModelInfo loadModelByFile(String filePath){
        return ModelReader.create().filePath(filePath).load();
    }

    public static ModelInfo loadModelByURL(String url, int timeout){
        return ModelReader.create().url(url).timeout(timeout).load();
    }

    public static ModelInfo loadModelByInputStream(InputStream inputStream){
        return ModelReader.create().inputStream(inputStream).load();
    }

    public static ModelInfo getModel(){
        return currentModelInfo;
    }

    public static void setModel(ModelInfo modelInfo){
        currentModelInfo = modelInfo;
    }
}
