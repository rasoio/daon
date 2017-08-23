import daon.spark.MakeModel;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.concurrent.*;

public class TestExecutor {

    Future<Boolean> running;

    @Test
    public void test() throws InterruptedException {

        ExecutorService tpool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());


        for(int i = 0; i< 10; i++) {
            Callable<Boolean> obj = () -> {
                System.out.println("hello");

                return true;
            };

            if(running == null || running.isDone()) {
                running = tpool.submit(obj);
            }
        }

    }
}
