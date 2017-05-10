package daon.manager.config;

import daon.analysis.ko.DaonAnalyzer2;
import daon.analysis.ko.DaonAnalyzer3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by mac on 2017. 4. 18..
 */
@Configuration
public class DaonConfig {

    @Bean
    public DaonAnalyzer3 analyzer() throws IOException {
        return new DaonAnalyzer3();
    }
}
