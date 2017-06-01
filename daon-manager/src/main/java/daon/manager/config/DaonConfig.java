package daon.manager.config;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.reader.ModelReader;
import daon.manager.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by mac on 2017. 4. 18..
 */
@Configuration
@ComponentScan(value = "daon.manager")
public class DaonConfig {

    @Autowired
    private ModelService modelService;

    @Bean
    public DaonAnalyzer analyzer() throws IOException {

        ModelInfo modelInfo = modelService.modelInfo();

        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(modelInfo);
        return daonAnalyzer;
    }
}
