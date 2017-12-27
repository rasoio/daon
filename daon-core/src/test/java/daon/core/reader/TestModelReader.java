package daon.core.reader;

import daon.core.result.ModelInfo;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by mac on 2017. 5. 29..
 */
public class TestModelReader {

    @Test
    public void readFile() throws IOException {

//        ModelInfo modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model2.dat").load();
        ModelInfo modelInfo = ModelReader.create().load();

    }


    @Test
    public void readUrl() throws IOException {

//        ModelInfo modelInfo = ModelReader.create().url("file:///Users/mac/work/corpus/model/model2.dat").load();
//        ModelInfo modelInfo = ModelReader.create().url("http://localhost:8082/static/model.dat").load();

    }
}
