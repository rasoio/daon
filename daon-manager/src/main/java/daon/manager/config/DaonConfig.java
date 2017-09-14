package daon.manager.config;

import daon.core.Daon;
import daon.core.model.ModelInfo;
import daon.manager.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by mac on 2017. 4. 18..
 */
@Configuration
@ComponentScan(value = "daon.manager")
public class DaonConfig {

    @Bean
    public Daon daon() throws IOException {
        return new Daon();
    }


    @Bean(destroyMethod="shutdownNow")
    public ExecutorService executorService(){

        return new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());
    }

}
