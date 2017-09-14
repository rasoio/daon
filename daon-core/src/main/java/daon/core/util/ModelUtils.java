package daon.core.util;

import daon.core.model.ModelInfo;
import daon.core.reader.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ModelUtils {

    private static ModelInfo currentModelInfo;

    static {
        //init
        init();
    }

    private static void init(){

        String filePath = System.getProperty("daon.model.file");
        String url = System.getProperty("daon.model.url");

        if(filePath != null){
            currentModelInfo = loadModelByFile(filePath);
        }else if(url != null){
            currentModelInfo = loadModelByURL(url);
        }else{
            currentModelInfo = loadModel();
        }
    }

    public static ModelInfo loadModel(){
        return ModelReader.create().load();
    }

    public static ModelInfo loadModelByFile(String filePath){
        return ModelReader.create().filePath(filePath).load();
    }

    public static ModelInfo loadModelByURL(String url){
        return ModelReader.create().url(url).load();
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
