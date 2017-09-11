package daon.core;

import daon.core.model.EojeolInfo;
import daon.core.model.Lattice;
import daon.core.model.ModelInfo;
import daon.core.processor.ConnectionProcessor;
import daon.core.processor.DictionaryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DaonAnalyzer{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private ModelInfo modelInfo;

    public DaonAnalyzer(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    public List<EojeolInfo> analyze(String text) throws IOException {

        if(text == null || text.length() == 0){
            return new ArrayList<>();
        }

        Lattice lattice = new Lattice(text);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice);

        return lattice.getEojeolInfos();
    }

    /*
    public List<EojeolInfo> analyze(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        //문장 단위 분리 처리
        StringReader input = new StringReader(text);

        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {

//                System.out.println(line);
                List<EojeolInfo> lineEojeolInfos = analyzeLine(line);

                eojeolInfos.addAll(lineEojeolInfos);
            }
        } catch (IOException exc) {
            // quit
        }
        //SegmentingTokenizerBase

//        int pos = 0;
//        int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
//
//        int IO_BUFFER_SIZE = 3;
//
//        CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);


        return eojeolInfos;
    }
    */

    public List<EojeolInfo> analyzeLine(String line) throws IOException {

        if(line == null || line.length() == 0){
            return new ArrayList<>();
        }

        Lattice lattice = new Lattice(line);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice);

        return lattice.getEojeolInfos();
    }

}
