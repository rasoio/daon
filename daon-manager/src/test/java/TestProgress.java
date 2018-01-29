import daon.manager.model.data.Progress;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

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

    @Test
    public void find() throws ClassNotFoundException {

        Class makeModel = Class.forName("daon.spark.model.MakeModel");

        System.out.println(makeModel);


    }

}
