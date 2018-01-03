import daon.spark.model.MakeModel;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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


    @Test
    public void test1(){
        List<String> list = new ArrayList<>();

        list.add("test1");
        list.add("test2");
        list.add("test3");

        System.out.println(list.contains("test12"));
    }
}
