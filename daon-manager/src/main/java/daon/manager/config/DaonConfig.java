package daon.manager.config;

import daon.analysis.ko.DaonAnalyzer2;
import daon.analysis.ko.DaonAnalyzer3;
import daon.analysis.ko.DaonAnalyzer4;
import daon.analysis.ko.model.loader.ModelLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by mac on 2017. 4. 18..
 */
@Configuration
public class DaonConfig {

    @Bean
    public DaonAnalyzer4 analyzer() throws IOException {

        ModelLoader loader = ModelLoader.create().load();

        DaonAnalyzer4 daonAnalyzer = new DaonAnalyzer4(loader.getFst(), loader.getModel());
        return daonAnalyzer;
    }
}
