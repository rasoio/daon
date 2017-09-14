package daon.core;

import daon.core.model.EojeolInfo;
import daon.core.model.Lattice;
import daon.core.model.ModelInfo;
import daon.core.processor.ConnectionProcessor;
import daon.core.processor.DictionaryProcessor;
import daon.core.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Daon {

    public Daon() {}

    public List<EojeolInfo> analyze(String sentence) throws IOException {

        char[] chars = sentence.toCharArray();
        int length = chars.length;

        return analyze(chars, length);
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

    public List<EojeolInfo> analyze(char[] chars, int length) throws IOException {

        if(chars == null || length == 0){
            return new ArrayList<>();
        }

        ModelInfo modelInfo = ModelUtils.getModel();

        Lattice lattice = new Lattice(chars, length);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice);

        return lattice.getEojeolInfos();
    }

}
