package daon.dictionary.main;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import daon.analysis.ko.dict.config.Config;
import daon.dictionary.model.Eojeol;
import daon.dictionary.model.Morpheme;
import daon.dictionary.model.Sentence;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class WriteJson {

    private Logger logger = LoggerFactory.getLogger(WriteJson.class);

    public static ObjectMapper mapper = new ObjectMapper();

    public void read() throws JsonParseException, JsonMappingException, IOException, InterruptedException, URISyntaxException {

        File jsonFile = new File("/Users/mac/Downloads/sejong2.json");

        //initialize
        FileUtils.write(jsonFile, "", "UTF-8");

        StopWatch watch = new StopWatch();

        watch.start();

        InputStream txtInput = getClass().getResourceAsStream("sejong.txt.gz");
        InputStream posInput = getClass().getResourceAsStream("sejong.pos.gz");

        List<String> txtLines = readLines(txtInput);
        List<String> posLines = readLines(posInput);

        //863043
        System.out.println(txtLines.size());

        int cnt = 0;

        for(int i=0, len = txtLines.size(); i<len; i++){
            String txt = txtLines.get(i);
            String pos = posLines.get(i);

            //공백 기준 분리
            String[] txtWord = txt.split("\\s+");
            String[] posWord = pos.split("\\s+");

            //문장
            Sentence sentence = new Sentence();
            sentence.setSentence(txt);

            List<Eojeol> eojeols = new ArrayList<>();

            //어절 간 연결 정보
            for(int subIdx=0, subLen = txtWord.length; subIdx < subLen; subIdx++){
                String subTxt = txtWord[subIdx];
                String subPos = posWord[subIdx];

                //어절 정보
                Eojeol eojeol = new Eojeol();
                eojeol.setSurface(subTxt);

                List<Morpheme> morphemes = new ArrayList<>();

                //?<= : Matches a group before the main expression without including it in the result.
                //?! : Specifies a group that can not match after the main expression (if it matches, the result is discarded).
                String[] morphs = subPos.split("(?<=[a-z][a-z])[+](?![/]s)");

                //어절 내 연결 정보
                for(String morph : morphs) {

                    //?= : Matches a group after the main expression without including it in the result.
                    String[] morphemeInfo = morph.split("[/](?=[a-z][a-z])");

                    //단어[0], posTag[1]
                    Morpheme morpheme = new Morpheme();
                    morpheme.setWord(morphemeInfo[0]);
                    morpheme.setTag(Config.POSTag.valueOf(morphemeInfo[1]));

                    morphemes.add(morpheme);
                }

                eojeol.setMorphemes(morphemes);
                eojeols.add(eojeol);

            }

            sentence.setEojeols(eojeols);


            //형태소 연결 설정
            setConnection(sentence);


//            sentences.add(sentence);


//          'INTER'	어절간 근접 확률
//          'INTRA'	어절내 근접 확률
//          'UNI'	형태소 출현 확률

            String json = mapper.writeValueAsString(sentence);


            FileUtils.write(jsonFile, json + System.lineSeparator(), "UTF-8", true);
        }


        /*
        json :

        {sentence:"징그럽다는건증오하곤다르다.",eojeols:[{surface:"징그럽다는",morphemes:[{word:"징그럽",tag:"vj"},{word:"다는",tag:"ed"}]},{surface:"건",morphemes:[{word:"것",tag:"nd"},{word:"ㄴ",tag:"px"}]},{surface:"증오하곤",morphemes:[{word:"증오",tag:"nc"},{word:"하고",tag:"pa"},{word:"ㄴ",tag:"px"}]},{surface:"다르다.",morphemes:[{word:"다르",tag:"vj"},{word:"다",tag:"ef"},{word:".",tag:"sf"}]}]}

        {
            sentence : "징그럽다는 건 증오하곤 다르다.",
            eojeols : [
                {
                    surface : "징그럽다는",
                    morphemes : [
                        {
                            word : "징그럽",
                            tag : "vj"
                        },
                        {
                            word : "다는",
                            tag : "ed"
                        }
                    ]
                },
                {
                    surface : "건",
                    morphemes : [
                        {
                            word : "것",
                            tag : "nd"
                        },
                        {
                            word : "ㄴ",
                            tag : "px"
                        }
                    ]
                },
                {
                    surface : "증오하곤",
                    morphemes : [
                        {
                            word : "증오",
                            tag : "nc"
                        },
                        {
                            word : "하고",
                            tag : "pa"
                        },
                        {
                            word : "ㄴ",
                            tag : "px"
                        }
                    ]
                },
                {
                    surface : "다르다.",
                    morphemes : [
                        {
                            word : "다르",
                            tag : "vj"
                        },
                        {
                            word : "다",
                            tag : "ef"
                        },
                        {
                            word : ".",
                            tag : "sf"
                        }
                    ]
                }
            ]
        }

        */

        System.out.println(cnt);


        watch.stop();

        System.out.println(watch.getTime() + " ms");

    }

    private List<String> readLines(InputStream input) throws IOException {

        InputStream gzipStream = new GZIPInputStream(input);

        try {
            return IOUtils.readLines(gzipStream, Charsets.toCharset("UTF-8"));
        } finally {
            IOUtils.closeQuietly(gzipStream);
        }
    }


    private void setConnection(Sentence sentence) {

        List<Eojeol> eojeols = sentence.getEojeols();

        for (int i = 0; i < eojeols.size(); i++) {
            Eojeol eojeol = eojeols.get(i);

            Morpheme prevOuter = null;
            Morpheme nextOuter = null;

            //첫번째 다음 어절
            if(i > 0){
                Eojeol prevEojeol = eojeols.get(i - 1);

                List<Morpheme> morphemes = prevEojeol.getMorphemes();

                //마지막 형태소
                prevOuter = morphemes.get(morphemes.size() -1).copy();
            }

            //마지막 이전 어절
            if((i + 1) < eojeols.size()){
                Eojeol nextEojeol = eojeols.get(i + 1);

                List<Morpheme> morphemes = nextEojeol.getMorphemes();

                //첫번째 형태소
                nextOuter = morphemes.get(0).copy();
            }

            List<Morpheme> morphemes = eojeol.getMorphemes();

            for (int j = 0; j < morphemes.size(); j++) {
                Morpheme morpheme = morphemes.get(j);

                Morpheme prevInner = null;
                Morpheme nextInner = null;

                /**
                 * 어절간 연결 설정
                 */
                //어절 시작 형태소인 경우
                if(j == 0) {
                    //이전 어절의 마지막 형태소가 어절 prev 로
                    morpheme.setPrevOuter(prevOuter);
                }

                //어절 마지막 형태소인 경우
                if((j + 1) == morphemes.size()){
                    //다음 어절의 시작 형태소가 어절 next 로
                    morpheme.setNextOuter(nextOuter);
                }


                /**
                 * 어절내 연결 설정
                 */
                //이전 형태소가 존재할 수 있는 경우
                if(j > 0){
                    prevInner = morphemes.get(j - 1).copy();
                }

                //다음 형태소가 존재할 수 있는 경우
                if((j + 1) < morphemes.size()){
                    nextInner = morphemes.get(j + 1).copy();
                }


                morpheme.setPrevInner(prevInner);
                morpheme.setNextInner(nextInner);

            }

        }



    }


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

//        float pb = (float) -Math.log((float) 150388 / (float) 22231026);
//        System.out.println(pb);

        WriteJson writeJson = new WriteJson();
        writeJson.read();

    }
}
