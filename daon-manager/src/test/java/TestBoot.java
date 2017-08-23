import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.Keyword;
import daon.manager.Application;
import daon.manager.model.data.AnalyzedEojeol;
import daon.manager.service.AnalyzeService;
import daon.manager.service.ModelService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by mac on 2017. 6. 7..
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class TestBoot {


    @Autowired
    private ModelService modelService;

    @Autowired
    private AnalyzeService analyzeService;

    @Test
    public void analyze() throws IOException {
        System.out.println("test");

        List<AnalyzedEojeol> results = analyzeService.analyze("정말 그대로 자라준\n" +
                "아역 출신 여배우의 일상");

        results.forEach(e->{
            System.out.println(e.getSurface());
            e.getTerms().forEach(t->{
                System.out.println(" '" + t.getSurface() + "'");
                for(Keyword k : t.getKeywords()) {
                    System.out.println("     " + k);
                }
            });
        });
    }


//    @Test
    public void download() throws IOException {
        System.out.println("test");

        byte[] bytes = modelService.model("1496794357651");

        FileOutputStream outputStream = new FileOutputStream("/Users/mac/work/corpus/model/model4.dat");

        IOUtils.write(bytes, outputStream);
    }

}
