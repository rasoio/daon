import com.fasterxml.jackson.databind.ObjectMapper;
import daon.analysis.ko.model.Keyword;
import daon.manager.Application;
import daon.manager.model.data.AnalyzedEojeol;
import daon.manager.model.data.Eojeol;
import daon.manager.model.data.Progress;
import daon.manager.service.AnalyzeService;
import daon.manager.service.CorpusService;
import daon.manager.service.ModelService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by mac on 2017. 6. 7..
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class TestParse {


    @Autowired
    private CorpusService corpusService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void parse() throws Exception {
        String value = readTestCase();

        List < Eojeol > results = corpusService.parse(value);

        String json = mapper.writeValueAsString(results);

        System.out.println(json);
    }

    private String readTestCase() throws URISyntaxException, IOException {
        URL url = this.getClass().getResource("testcase.txt");

        File testcase = new File(url.toURI());

        return FileUtils.readFileToString(testcase, "UTF-8");
    }

}
