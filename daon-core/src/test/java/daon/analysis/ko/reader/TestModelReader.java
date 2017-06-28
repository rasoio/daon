package daon.analysis.ko.reader;

import daon.analysis.ko.model.ModelInfo;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by mac on 2017. 5. 29..
 */
public class TestModelReader {

    @Test
    public void readFile() throws IOException {

        ModelInfo modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model2.dat").load();

    }


    @Test
    public void readUrl() throws IOException {

//        ModelInfo modelInfo = ModelReader.create().url("file:///Users/mac/work/corpus/model/model2.dat").load();
//        ModelInfo modelInfo = ModelReader.create().url("http://localhost:8082/static/model.dat").load();

    }
}
