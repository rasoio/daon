package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.processor.ConnectionFinder;
import daon.analysis.ko.reader.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;

public class TestArcModel {


    private Logger logger = LoggerFactory.getLogger(TestArcModel.class);

    private ModelInfo modelInfo;

    private DaonAnalyzer daonAnalyzer;

    @Before
    public void before() throws IOException {

//        modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model7.dat").load();
        modelInfo = ModelReader.create().load();

        daonAnalyzer = new DaonAnalyzer(modelInfo);
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
//        String sentence = "하늘을 나는 비행기";
//        String sentence = "원인 파악됐을까요?";
//        String sentence = "안먹어 안돼";
//        String sentence = "날더러 사괄 먹었다 엉겨든다니깐 ";
//        String sentence = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
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
//        String sentence = "고속 도로 휴게소 성묘지 등에서 특별 정비 ";
//        String sentence = "안된다고절실히";
//        String sentence = "근데 밤이 이슥할 무렵 그 친구는 갑자기 \"포르노 안 볼래?\" 하고 물었다.)";
//        String sentence = "'엑스포 70'의 일본 박람회만 해도 그렇지 않았던가!)";
//        String sentence = "오로지 그만이 분명한 것으로 보게 된다.";
//        String sentence = "집으로 찾아오시면 체온한번 재드릴께욤,,"; //재드릴께욤
//        String sentence = "재드릴께요"; //재드릴께욤 <=== ***
//        String sentence = "수고하셧습니다.";
//        String sentence = "재 드릴게요"; //재드릴께욤
//        String sentence = "재승인"; //재드릴께욤
//        String sentence = "드릴께요"; //재드릴께욤
//        String sentence = "재"; //재드릴께욤
//        String sentence = "드릴"; //재드릴께욤
//        String sentence = "께욤"; //재드릴께욤
//        String sentence = "오현님 가져오시죠. 집에서.."; //가져오시죠...
//        String sentence = "두 가지 세 가지";
//        String sentence = "아침에 나는 아버지하고 한의원에 진찰하러 갔다.";
//        String sentence = "나스스로와 가족에게 각인시키고 싶었기 때문";
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
//        String sentence = "화장품이 화장품이다";
//        String sentence = "남성이월등산복";
        String sentence = "따듯한 차를 따라라";
//        String sentence = "40%";
//        String sentence = "나는 오히려 이렇듯 즐거움에 익숙한(즐거움에 길들여진 것이 아니라 매력 있는 즐거움을 발견·발굴·발명할 줄 아는) 신세대의 왕성한 에너지를 국가 발전의 원동력으로 삼투시키는 방법에 대해 진지한 논의를 벌이길 바란다.";
//        String sentence = "나이키운동화아디다스";
//        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "그네들과 수작을 건네는 사이에 종소리는 어느덧 그쳐 있었다.";
//        String sentence = "a.5kg 다우니운동화 남자지갑♧";
//        String sentence = "하늘을 나는 비행기";
//        String sentence = "소년은 캄캄한 방에 혼자 남자 덜컥 겁이 났다.";


       List<EojeolInfo> eojeolInfos = daonAnalyzer.analyzeText(sentence);

       eojeolInfos.forEach(e->{
           System.out.println(e.getEojeol());
           e.getTerms().forEach(t->{
               System.out.println(" '" + t.getSurface() + "' (" + t.getOffset() + ":" + (t.getOffset() + t.getLength()) + ") arc : " + t.getArc());
               for(Keyword k : t.getKeywords()) {
                   System.out.println("     " + k.getSeq() + ", " + k);
               }
           });
       });




    }

    @Test
    public void testTagTransScore(){
        checkTagTransScore(POSTag.NNG, POSTag.VX);
        checkTagTransScore(POSTag.NNG, POSTag.VV);
        checkTagTransScore(POSTag.VV, POSTag.EC);
        checkTagTransScore(POSTag.EC, POSTag.VV);
    }


    @Test
    public void testConnectionFinder(){


        ConnectionFinder finder = new ConnectionFinder(modelInfo.getConnFst());

        Arc before = finder.initArc();

//        이
//        '이' (0:1) arc : null
//        183531, (seq : 183531, word : 이, tag : MM, freq : 44975)
//        시에서
//        '시에서' (0:3) arc : {state=FINAL, arc=node=16517319 target=16517410 label=0x26beb final arcArray(idx=8 of 11), cnt=3}
//        139349, (seq : 139349, word : 시, tag : NNG, freq : 4998)
//        158699, (seq : 158699, word : 에서, tag : JKB, freq : 139509)
//        율곡은
//        '율곡은' (0:3) arc : null
//        180621, (seq : 180621, word : 율곡, tag : NNP, freq : 147)
//        180963, (seq : 180963, word : 은, tag : JX, freq : 252910)
//        물고기가
//        '물고기가' (0:4) arc : {state=FINAL, arc=node=52361612 target=-1 label=0x5c2 final last stop, cnt=4}
//        84422, (seq : 84422, word : 물고기, tag : NNG, freq : 420)
//        1474, (seq : 1474, word : 가, tag : JKS, freq : 224602)
//        물에서
//        '물에서' (0:3) arc : null
//        84343, (seq : 84343, word : 물, tag : NNG, freq : 5727)
//        158699, (seq : 158699, word : 에서, tag : JKB, freq : 139509)
//        뛰고
//        '뛰고' (0:2) arc : {state=FINAL, arc=node=52347359 target=-1 label=0x3867 final stop, cnt=4}
//        68494, (seq : 68494, word : 뛰, tag : VV, freq : 1872)
//        14439, (seq : 14439, word : 고, tag : EC, freq : 312362)

//        int[] seqs = new int[]{183531, 139349, 158699};
//        int[] seqs = new int[]{257090, 181422, 41891, 50758};
        int[] seqs = new int[]{257090, 181422, 43467, 50758, 93646, 181422};

        for (int i = 0; i < seqs.length; i++) {
            int seq = seqs[i];
            Arc after = finder.find(seq, before);

            logger.info("seq : {}, after : {}, before : {}", seq, after, before);

            before = after;

        }

    }

    private void checkTagTransScore(POSTag t1, POSTag t2){

        Keyword k1 = new Keyword(0, "", t1);
        Keyword k2 = new Keyword(0, "", t2);

        float score = modelInfo.getTagScore(t1.name(), t2.name());
        logger.info("{}, {} => score : {}", t1, t2, score);
    }


    public List<EojeolInfo> read() throws IOException, InterruptedException {
        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
        List<EojeolInfo> eojeolInfos = daonAnalyzer.analyzeText(sentence);
        return eojeolInfos;
    }


}
