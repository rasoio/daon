package daon.analysis.ko;

import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.Lattice;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.processor.ConnectionProcessor;
import daon.analysis.ko.processor.DictionaryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class DaonAnalyzer implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private ModelInfo modelInfo;


    public DaonAnalyzer(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    public List<EojeolInfo> analyzeText(String text) throws IOException {

        Lattice lattice = new Lattice(text);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice);

        List<EojeolInfo> eojeolInfos = lattice.getEojeolInfos();

        return eojeolInfos;
    }

}
