import com.fasterxml.jackson.databind.ObjectMapper;
import daon.core.data.Eojeol;
import daon.core.util.Utils;
import daon.manager.Application;
import daon.manager.service.SentenceService;
import org.apache.commons.io.FileUtils;
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
    private SentenceService sentenceService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void parse() throws Exception {
        String value = readTestCase();

        List<Eojeol> results = Utils.parse(value);

        String json = mapper.writeValueAsString(results);

        System.out.println(json);
    }

    private String readTestCase() throws URISyntaxException, IOException {
        URL url = this.getClass().getResource("testcase.txt");

        File testcase = new File(url.toURI());

        return FileUtils.readFileToString(testcase, "UTF-8");
    }

}
