import com.fasterxml.jackson.databind.ObjectMapper;
import daon.manager.Application;
import daon.manager.model.data.Eojeol;
import daon.manager.model.data.Progress;
import daon.manager.service.CorpusService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by mac on 2017. 6. 7..
 */

public class TestProgress {



    @Test
    public void progress() throws InterruptedException {
        Progress progress = new Progress();
        progress.setNumCompletedJobs(8);

        int p = progress.getProgress();

        System.out.println(p);

        float a = 36;
        int b = 8;

        int r = (int) ((b/a) * 100);
        System.out.println(r);



        StopWatch stopWatch = StopWatch.createStarted();

        Thread.sleep(1000);
        stopWatch.split();

        long time = stopWatch.getTime();
        System.out.println(time);

        Thread.sleep(1000);
        stopWatch.split();

        time = stopWatch.getTime();
        System.out.println(time);

        stopWatch.stop();

        time = stopWatch.getTime();
        System.out.println(time);


    }

}
