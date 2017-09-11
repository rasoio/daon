package daon.core.model;

import daon.core.DaonAnalyzer;
import daon.core.reader.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestArcModel {

    private Logger logger = LoggerFactory.getLogger(TestArcModel.class);

    private ModelInfo modelInfo;

    private DaonAnalyzer daonAnalyzer;

    @Before
    public void before() throws IOException {

//        modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model7.dat").load();
        modelInfo = ModelReader.create().load();

        daonAnalyzer = new DaonAnalyzer(modelInfo);
    }

    public DaonAnalyzer getDaonAnalyzer() {
        return daonAnalyzer;
    }

    @Test
    public void test1() throws IOException, InterruptedException {

    }


}
