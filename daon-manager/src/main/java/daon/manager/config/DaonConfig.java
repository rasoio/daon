package daon.manager.config;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.reader.ModelReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by mac on 2017. 4. 18..
 */
@Configuration
public class DaonConfig {

    @Bean
    public DaonAnalyzer analyzer() throws IOException {

        ModelInfo modelInfo = ModelReader.create().load();

        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(modelInfo);
        return daonAnalyzer;
    }
}
