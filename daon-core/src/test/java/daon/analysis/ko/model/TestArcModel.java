package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.processor.ConnectionFinder;
import daon.analysis.ko.reader.ModelReader;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PairOutputs;
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



//        String sentence = "그래서 1율곡은 선조에게 12ABS";
//        String sentence = "이시에서율곡은물고기가물에서뛰고솔개가하늘을나는현상";
//        String sentence = "이 시에서 율곡은 물고기가 물에서 뛰고 솔개가 하늘을 나는 현상";
//        String sentence = "하늘을 나는 비행기"; // 나는
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
//        String sentence = "아버지가 방에 들어가신다";
//        String sentence = "나는 새들의";
//        String sentence = "나는 그 천식 같은 소리에서 박자도 가려낼 수가 없었지만 한번 배그파이프에 익숙해지면 북처럼 쉽게 박자를 분별할 수 있는 모양이다.";
//        String sentence = "인간의 존재와 본능을 이처럼 양극적인 2원 구조로 파악하는 이들 연구자들의 견해는 어릴 때부터 인간을 `내 편, 네 편' `좋은 사람, 나쁜 사람'으로 가르는 2분법적 사고에 익숙하도록 길들여져 온 우리들에게 결국 인간이란 지킬 박사와 하이드와 같은 존재라는 점, 상황에 따라 `야수'가 되기도 하고 `사람'이 되기도 하는 양면적 존재라는 사실을 새삼 일깨워 준다.";
//        String sentence = "견물생심이라고,"; // 이, 이, 라고
//        String sentence = "소매달린 집중단속하기로 아이패드를 등록했을런가?";
//        String sentence = "사람인가";
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
//        String sentence = "재드릴께요"; //재드릴께욤 <==  ***
//        String sentence = "수고하셧습니다.";
//        String sentence = "재 드릴게요"; //재드릴께욤
//        String sentence = "2승인 경기"; //재드릴께욤
//        String sentence = "재승인받아라"; //재드릴께욤
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
//        String sentence = "화장품이";
//        String sentence = "화장품이 화장품이다"; //화장품, 이 VCP, 다 EF
//        String sentence = "자료이다.";
//        String sentence = "이이다";
//        String sentence = "화장품이다";
//        String sentence = "11월28일";
//        String sentence = "남성이월등산복";
//        String sentence = "따듯한 차를 따라라";
//        String sentence = "40%";
//        String sentence = "나는 오히려 이렇듯 즐거움에 익숙한(즐거움에 길들여진 것이 아니라 매력 있는 즐거움을 발견·발굴·발명할 줄 아는) 신세대의 왕성한 에너지를 국가 발전의 원동력으로 삼투시키는 방법에 대해 진지한 논의를 벌이길 바란다.";
//        String sentence = "나이키운동화아디다스";
//        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "그네들과 수작을 건네는 사이에 종소리는 어느덧 그쳐 있었다.";
//        String sentence = "한편 신기술사업투자조합의 경우에는 조합의 결성 실적이 부진하여 1999년 말 잔액 기준으로 962억 원의 저조한 투자 실적을 나타내고 있다.";
//        String sentence = "그러나 이러한 것이 결국 자산을 모으는 길로 나의 발길을 들여놓게 하고 있었다.";
//        String sentence = "가만히 내버려 두어도 태아는 저절로 한 바퀴 돌면서 나온다.";
//        String sentence = "어느 일방만이 발표를 하게 될 때는 사전 협의가 필요하다는 것 역시 일반적인 상식이다."; // 일방만이
//        String sentence = "어느 일방만이"; // 일방만이
//        String sentence = "중얼거리며 잰걸음질을 치기 시작했다."; // 치기
//        String sentence = "배를 타고 "; // 배를
//        String sentence = "미디어는 우편, "; // 우편
//        String sentence = "컴퓨터는 특히 문자정보, 음성정보, 화상정보를 동시에 처리함으로써 멀티미디어로 발전하고 있다."; // 문자정보
//        String sentence = "다혈질의 기분을 억누르는 데도, 유달리 열기가 많은 몸을 식히는 데도 그 편이 나았으니까."; //편이
//        String sentence = "이는 20여개월 전인 2000년 11월28일 1181.70원 이후 최저치다."; //이는 -> conn 수치
//        String sentence = "이는 20여개월"; //이는 -> conn 수치
//        String sentence = "나타내고 있다."; //있다.
//        String sentence = "그렇지만 새 음반을 낸 이유는 "; //새
//        String sentence = "그러나 내용상의 혁명에 대한 이 책에서의 논의는 위의 세 분야로서 마치기로 한다."; //한다 -> 하 VV (O), 하 VX (X) + 었/EP
        String sentence = "노 후보는 지난 4일 '탈 디제이' 기자회견과 관련해 \"구체적인 후속 프로그램은 가능한 것이 없다\"며 \"다만 한나라당이 특검제를 하자고 정치공세를 할 때 민주당이 기자회견 내용을 활용해 대응할 수 있을 것\"이라고 말했다."; //한다
//        String sentence = "동학의 접은 생태적 공생공동체(계·두레·친인척)이지만 도소(都所)와 포(包)로부터의 통문이 원심적으로 확산하되 접꾼 개개인의 영적 생명의 질적 성취를 통해서 무궁확산하는 기이한 소통공동체다."; //공생공동체
//        String sentence = "생태적 공생공동체(계·두레·친인척)이지만 도소(都所)와 포(包)로부터의 통문이 원심적으로 확산하되 접꾼 개개인의 영적 생명의 질적 성취를 통해서 무궁확산하는 기이한 소통공동체다."; //공생공동체

//        String sentence = "공생공동체"; //공생공동체
//        String sentence = "그 편이 나았으니까."; //편이
//        String sentence = "술래가 한 바퀴";
//        String sentence = "a.5kg 다우니운동화 남자지갑♧";
//        String sentence = "하늘을 나는 비행기";
//        String sentence = "소년은 캄캄한 방에 혼자 남자 덜컥 겁이 났다.";
//        String sentence = "거슬러 내려가셨다";


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
    public void testFindForwardFst() throws IOException {

        DaonFST<Object> fst = modelInfo.getForwardFst();

        final FST.BytesReader fstReader = fst.getBytesReader();

        String test = "드려요";

        final char[] chars = test.toCharArray();
        final int charsLength = test.length();

//        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<Object> arc = new FST.Arc<>();

        for (int offset = 0; offset < charsLength; offset++) {
            arc = fst.getFirstArc(arc);
            Object output = fst.getOutputs().getNoOutput();
            int remaining = charsLength - offset;

            Object outputs = null;
            int lastIdx = 0;

            for (int i = 0; i < remaining; i++) {
                int ch = chars[offset + i];

                //탐색 결과 없을때
                if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
                    break; // continue to next position
                }

                //탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
                output = fst.getOutputs().add(output, arc.output);

                // 매핑 종료
                if (arc.isFinal()) {

                    //사전 매핑 정보 output
                    outputs = fst.getOutputs().add(output, arc.nextFinalOutput);

                    lastIdx = i;
                }

            }

            if(outputs != null){

                List<PairOutputs.Pair<Long,IntsRef>> list = fst.asList(outputs);

                //표층형 단어
                final String word = new String(chars, offset, (lastIdx + 1));

                final int length = (lastIdx + 1);

                //디버깅용 로깅
                if(logger.isDebugEnabled()) {
                    logger.debug("word : {}, offset : {}, end : {}, find cnt : ({})", word, offset, (offset + length), list.size());

                    debugWords(list);

                }
            }

            // 매칭 종료 시점
            offset += lastIdx;
        }

        logger.debug("forward end !!");


    }


    @Test
    public void testFindBackwardFst() throws IOException {

        DaonFST<Object> fst = modelInfo.getBackwardFst();

        final FST.BytesReader fstReader = fst.getBytesReader();

        String test = "재드릴께요";

        final char[] chars = test.toCharArray();
        final int charsLength = test.length();

//        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<Object> arc = new FST.Arc<>();

        for (int offset = charsLength - 1; offset > 0; offset--) {
            arc = fst.getFirstArc(arc);
            Object output = fst.getOutputs().getNoOutput();
            int remaining = offset;

            Object outputs = null;
            int lastIdx = 0;

            for (int i = 0; i < remaining; i++) {
                int ch = chars[offset - i];

                logger.info("find : {}", chars[offset - i]);

                //탐색 결과 없을때
                if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
                    break;
//                    return lastOffset;
                }

                //탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
                output = fst.getOutputs().add(output, arc.output);

                // 매핑 종료
                if (arc.isFinal()) {

                    //사전 매핑 정보 output
                    outputs = fst.getOutputs().add(output, arc.nextFinalOutput);
                    lastIdx = i;
                }
            }

            if(outputs != null){

                List<PairOutputs.Pair<Long,IntsRef>> list = fst.asList(outputs);
                int wordOffset = offset - lastIdx;

                //표층형 단어
                final String word = new String(chars, wordOffset, (lastIdx + 1));

                final int length = (lastIdx + 1);

                //디버깅용 로깅
                if(logger.isDebugEnabled()) {
                    logger.debug("word : {}, offset : {}, end : {}, find cnt : ({})", word, offset, (offset + length), list.size());

                    debugWords(list);
                }

            }

            // 매칭 종료 시점
            offset -= lastIdx;

        }

        logger.debug("backward end !!");


    }

    private void debugWords(List<PairOutputs.Pair<Long, IntsRef>> list) {
        list.sort((p1, p2) -> p2.output1.compareTo(p1.output1));

        for (PairOutputs.Pair<Long, IntsRef> pair : list) {
            List sb = new ArrayList();

            IntStream.of(pair.output2.ints).forEach(seq -> {

                Keyword k = modelInfo.getKeyword(seq);

                sb.add(k);
            });

            logger.debug("  freq : {}, keywords : {}", pair.output1, sb);
        }
    }

    @Test
    public void testTagTransScore(){
        checkTagTransScore(POSTag.NNG, POSTag.VX);
        checkTagTransScore(POSTag.NNG, POSTag.VV);
        checkTagTransScore(POSTag.VV, POSTag.EC);
        checkTagTransScore(POSTag.EC, POSTag.VV);
    }


    @Test
    public void testInnerFinder() {


        ConnectionFinder finder = new ConnectionFinder(modelInfo);

        Long innerFreq = finder.findInner(190391,93391);

        logger.info("inner freq : {}", innerFreq);
    }

    @Test
    public void testOuterFinder() {


        ConnectionFinder finder = new ConnectionFinder(modelInfo);

//        Long outerFreq = finder.findOuter(31380,119772);

//        logger.info("outer freq : {}", outerFreq);
    }


    @Test
    public void testConn() throws IOException {

        ConnectionFinder finder = new ConnectionFinder(modelInfo);

        Term t1 = createTerm(257090, 181422);
//        Term t1 = createTerm(94584, 71658);
        Term t2 = createTerm(43467);

        Long freq = finder.findConn(t1, t2);

        logger.info("freq : {}", freq);
    }

    @Test
    public void testTagTrans(){
        POSTag t1 = POSTag.XSN;
        POSTag t2 = POSTag.NNG;

        String t = t1.name() + "|END";

        float score = modelInfo.getTagScore(t, t2.name());

        logger.info("score : {}", score);
    }

    @Test
    public void testTagTrans1(){
        POSTag t1 = POSTag.FIRST;
        POSTag t2 = POSTag.MAG;

        float score = modelInfo.getTagScore(t1.name(), t2.name());

        logger.info("score : {}", score);
    }


    private Term createTerm(int... seqs){

        List<Keyword> keywords = new ArrayList<>();

        for(int seq : seqs){
            keywords.add(new Keyword(seq, "", POSTag.NNG));
        }

        return new Term(0,0, "", MatchType.UNKNOWN, 0, keywords.toArray(new Keyword[0]));
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
