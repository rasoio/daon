package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer2;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.Arc.State;
import daon.analysis.ko.processor.ConnectionFinder;
import daon.analysis.ko.processor.DictionaryProcessor;
import daon.analysis.ko.processor.UnknownProcessor;
import daon.analysis.ko.reader.JsonFileReader;
import daon.analysis.ko.reader.ModelReader;
import daon.analysis.ko.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.PairOutputs.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;

public class TestArcModel {


    private Logger logger = LoggerFactory.getLogger(TestArcModel.class);

    private ModelInfo modelInfo;

    private DaonAnalyzer2 daonAnalyzer2;

    @Before
    public void before() throws IOException {

        modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model6.dat").load();

        daonAnalyzer2 = new DaonAnalyzer2(modelInfo);
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        read();
    }

    @Test
    public void test2() throws IOException, InterruptedException {


//        그래서
//        그래서 : 31209, 31210, [31363, 156534], [31271, 156534]
//
//        율곡은
//        율곡 : 180610, 180611
//        은 : 180954, 180953, 180955
//        율곡은 : [180611, 180953], [180610, 180953]
//
//        선조에게
//        선조 : 125589, 125590
//        에게 : 158332, 158331
//        선조에게 : [125589, 158332]

//        ResultInfo resultInfo = ResultInfo.create("그래서 율곡은 선조에게".toCharArray(), 12);
//
//        resultInfo.addCandidateTerm(createTerm(0, 3, "그래서", 31209));
//        resultInfo.addCandidateTerm(createTerm(0, 3, "그래서", 31210));
//        resultInfo.addCandidateTerm(createTerm(0, 3, "그래서", 31363, 156534));
//        resultInfo.addCandidateTerm(createTerm(0, 3, "그래서", 31271, 156534));
//
//        resultInfo.addCandidateTerm(createTerm(4, 2, "율곡", 180610));
//        resultInfo.addCandidateTerm(createTerm(4, 2, "율곡", 180611));
//        resultInfo.addCandidateTerm(createTerm(4, 3, "율곡은", 180611, 180953));
//        resultInfo.addCandidateTerm(createTerm(4, 3, "율곡은", 180610, 180953));
//        resultInfo.addCandidateTerm(createTerm(6, 1, "은", 180954));
//        resultInfo.addCandidateTerm(createTerm(6, 1, "은", 180953));
//        resultInfo.addCandidateTerm(createTerm(6, 1, "은", 180955));
//
//        resultInfo.addCandidateTerm(createTerm(8, 2, "선조", 125589));
//        resultInfo.addCandidateTerm(createTerm(8, 2, "선조", 125590));
//        resultInfo.addCandidateTerm(createTerm(8, 4, "선조에게", 125589, 158332));
//        resultInfo.addCandidateTerm(createTerm(10, 2, "에게", 158332));
//        resultInfo.addCandidateTerm(createTerm(10, 2, "에게", 158331));



//        String sentence = "그래서1율곡은 선조에게 12ABS";
//        String sentence = "이시에서율곡은물고기가물에서뛰고솔개가하늘을나는현상";
//        String sentence = "이 시에서 율곡은 물고기가 물에서 뛰고 솔개가 하늘을 나는 현상";
//        String sentence = "솔개가 하늘을 나는 현상";
//        String sentence = "그런데 정작 중국에서 golang.org는 황금방패 혹은 만리장성으로 불리우는 중국 정부의 방화벽에 차단을 먹었다";
//        String sentence = "프로그램의 현재 상태에 비추어 앞으로 실행해야 할 나머지 코드들을 의미한다";
//        String sentence = "그는";
//        String sentence = "코드들을 의미한다";
//        String sentence = "나는 익숙해진 코스대로 한 곳에 앉아서 동트는 새벽 하늘을 가로막은 안개의 흰 베일이 걷히기를 기다렸다.";
//        String sentence = "아버지가방에들어가신다";
//        String sentence = "나는 새들의";
//        String sentence = "나는 그 천식 같은 소리에서 박자도 가려낼 수가 없었지만 한번 배그파이프에 익숙해지면 북처럼 쉽게 박자를 분별할 수 있는 모양이다.";
//        String sentence = "인간의 존재와 본능을 이처럼 양극적인 2원 구조로 파악하는 이들 연구자들의 견해는 어릴 때부터 인간을 `내 편, 네 편' `좋은 사람, 나쁜 사람'으로 가르는 2분법적 사고에 익숙하도록 길들여져 온 우리들에게 결국 인간이란 지킬 박사와 하이드와 같은 존재라는 점, 상황에 따라 `야수'가 되기도 하고 `사람'이 되기도 하는 양면적 존재라는 사실을 새삼 일깨워 준다.";
//        String sentence = "견물생심이라고,"; // 이, 이, 라고
//        String sentence = "소매달린 집중단속하기로 아이패드를 등록했을런가?";
//        String sentence = "오늘 지마켓/옥션 가격 변동 많습니다."; //지마켓
//        String sentence = "지마켓/옥션"; //지마켓
//        String sentence = "그리고";
//        String sentence = "오른쪽꺼는 너무 작아?"; //지마켓
//        String sentence = "아침에 자전거 타고 오는데 포기할뻔";
//        String sentence = "오다 쓰러지는줄 알았어";
//        String sentence = "집으로 찾아오시면 체온한번 재드릴께욤,,"; //재드릴께욤
//        String sentence = "재드릴께요"; //재드릴께욤
//        String sentence = "재 드릴게요"; //재드릴께욤
//        String sentence = "재승인"; //재드릴께욤
//        String sentence = "드릴께요"; //재드릴께욤
//        String sentence = "재"; //재드릴께욤
//        String sentence = "드릴"; //재드릴께욤
//        String sentence = "께욤"; //재드릴께욤
//        String sentence = "오현님 가져오시죠. 집에서.."; //재드릴께욤
//        String sentence = "그래서 더 괴로워하고 그래서 더 술에 취해 살려고 하는 것 같았다.";
//        String sentence = "24일 200만~300만원에";
//        String sentence = "결혼했어야";
//        String sentence = "일일지대계一日之大計다.,";
//        String sentence = "[단독]검찰 견제할 독립 기구 ‘변호처’ 만든다";
//        String sentence = "소문 듣고 샀는데 정말 좋았던 화장품이 있다면 소개해주세요\n" +
//                "올리브영 세일 기간에 구매했던\n" +
//                "존프리다 6 effect 세럼이예요. \n" +
//                "[출처] 소문 듣고 샀는데 정말 좋았던 화장품이 있다면 소개해주세요|작성자 임냥\n";
//        String sentence = "술에 취해";
        String sentence = "화장품이 화장품이다";
//        String sentence = "40%";
//        String sentence = "나는 오히려 이렇듯 즐거움에 익숙한(즐거움에 길들여진 것이 아니라 매력 있는 즐거움을 발견·발굴·발명할 줄 아는) 신세대의 왕성한 에너지를 국가 발전의 원동력으로 삼투시키는 방법에 대해 진지한 논의를 벌이길 바란다.";
//        String sentence = "나이키운동화아디다스";
//        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "그네들과 수작을 건네는 사이에 종소리는 어느덧 그쳐 있었다.";
//        String sentence = "a.5kg 다우니운동화 남자지갑♧";
//        String sentence = "하늘을 나는 비행기";
//        String sentence = "소년은 캄캄한 방에 혼자 남자 덜컥 겁이 났다.";


       List<EojeolInfo> eojeolInfos = daonAnalyzer2.analyzeText(sentence);

       eojeolInfos.forEach(e->{
           System.out.println(e.getEojeol());
           e.getTerms().forEach(t->{
               System.out.println(" '" + t.getSurface() + "' (" + t.getOffset() + ":" + (t.getOffset() + t.getLength()) + ") arc : " + t.getArc());
               for(Keyword k : t.getKeywords()) {
                   System.out.println("     " + k.getSeq() + ", " + k);
               }
           });
       });


       checkTagTransScore(POSTag.NNG, POSTag.VX);
       checkTagTransScore(POSTag.NNG, POSTag.VV);
       checkTagTransScore(POSTag.VV, POSTag.EC);
       checkTagTransScore(POSTag.EC, POSTag.VV);


       ConnectionFinder finder = new ConnectionFinder(modelInfo.getConnFst());

       Arc init = finder.initArc();

       Arc a1 = finder.find(271281, init);
       Arc a2 = finder.find(139349, a1);
       Arc a3 = finder.find(158699, a2);
       Arc a4 = finder.find(180621, init);
       Arc a5 = finder.find(180963, a4);


       logger.info("a1 : {}, a2 : {}, a3 : {}, a4 : {}, a5 : {}", a1, a2, a3, a4, a5);
    }

    private void checkTagTransScore(POSTag t1, POSTag t2){

        Keyword k1 = new Keyword(0, "", t1);
        Keyword k2 = new Keyword(0, "", t2);

        float score = modelInfo.getTagScore(k1, k2);
        logger.info("{}, {} => score : {}", t1, t2, score);
    }


    public List<EojeolInfo> read() throws IOException, InterruptedException {
        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
        List<EojeolInfo> eojeolInfos = daonAnalyzer2.analyzeText(sentence);
        return eojeolInfos;
    }


}
