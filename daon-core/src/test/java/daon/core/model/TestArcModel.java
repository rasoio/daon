package daon.core.model;

import daon.core.Daon;
import daon.core.reader.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestArcModel {

    private Logger logger = LoggerFactory.getLogger(TestArcModel.class);

    private Daon daon;

    @Before
    public void before() throws IOException {

        daon = new Daon();
    }

    public Daon getDaon() {
        return daon;
    }

    @Test
    public void test1() throws IOException, InterruptedException {

    }


}
