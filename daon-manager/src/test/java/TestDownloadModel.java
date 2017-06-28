import daon.manager.Application;
import daon.manager.service.ModelService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mac on 2017. 6. 7..
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class TestDownloadModel {


    @Autowired
    private ModelService modelService;

    @Test
    public void download() throws IOException {
        System.out.println("test");

        byte[] bytes = modelService.model("1496794357651");

        FileOutputStream outputStream = new FileOutputStream("/Users/mac/work/corpus/model/model4.dat");

        IOUtils.write(bytes, outputStream);


    }

}
