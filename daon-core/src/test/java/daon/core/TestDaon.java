package daon.core;

import daon.core.data.Eojeol;
import daon.core.handler.EchoHandler;
import daon.core.reader.ModelReader;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.ModelInfo;
import daon.core.util.ModelUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class TestDaon {


    private Logger logger = LoggerFactory.getLogger(TestDaon.class);

    private Daon daon;

    @Before
    public void before() throws IOException {

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.WARN);

        daon = new Daon();
    }

    @Test
    public void test1() throws IOException, InterruptedException {

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
//        String sentence = "드릴께요"; //재드릴께욤 <==  ***
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
////        String sentence = "술에 취해";
//        String sentence = "화장품이";
//        String sentence = "화장품이 화장품이다"; //화장품, 이 VCP, 다 EF
//        String sentence = "자료이다.";
//        String sentence = "이이다";
//        String sentence = "화장품이다";
//        String sentence = "11월28일";
//        String sentence = "윤숙경";
//        String sentence = "3단계를";
//        String sentence = "그 뜻을 한 번 더 찾아봐야겠어.";
//        String sentence = "11번가";
//        String sentence = "남성이월등산복";
//        String sentence = "남성 이월 등산복";
//        String sentence = "따듯한 차를 따라라";
//        String sentence = "40%";
//        String sentence = "예를 들어, To ski is fun.(스키";
//        String sentence = "3. 드 클레랑보 신드롬(de Clerambault syndrome)";
//        String sentence = "사실 성의";
//        String sentence = "있으며,";
//        String sentence = "그곳에 커다란 호기심을 남겨 두고 그대가 다시 지팡이를 끌고 오른손 쪽으로 대동강을 바라다보면서 청류벽(淸流壁)을 끼고 부벽루(浮碧樓)까지 올라가 거기서 다시 모란봉으로 -- 또 돌아서면서 을밀대(乙密臺)로, 을밀대에서 기자(箕子) 묘송림(墓松林)으로, 현무문(玄武門)으로 -- 우리의 없는 조상을 위하여 옷깃을 눈물로 적시며 혹은 회고의 염(念)에 한숨을 지으며 왕손(王孫)은 거불귀(去不歸) 옛날의 시(詩)를 통절히 느끼면서 돌아본 뒤에 다시 시가로 향하여 내려온다고 하자.";
//        String sentence = "또 돌아서면서 을밀대(乙密臺)로, 을밀대에서 기자(箕子) 묘송림(墓松林)으로, 현무문(玄武門)으로 -- 우리의 없는 조상을 위하여 옷깃을 눈물로 적시며 혹은 회고의 염(念)에 한숨을 지으며 왕손(王孫)은 거불귀(去不歸) 옛날의 시(詩)를 통절히 느끼면서 돌아본 뒤에 다시 시가로 향하여 내려온다고 하자.";
//        String sentence = "또 돌아서면서 을밀대(乙密臺)로, 을밀대에서 기자(箕子) 묘송림(墓松林)으로, 현무문(玄武門)으로 --";
//        String sentence = "우리의 없는 조상을 위하여 옷깃을 눈물로 적시며 혹은 회고의 염(念)에 한숨을 지으며 왕손(王孫)은 거불귀(去不歸) 옛날의 시(詩)를 통절히 느끼면서 돌아본 뒤에 다시 시가로 향하여 내려온다고 하자.";
//        String sentence = "Clerambault";
//        String sentence = "율똥뿡다음꽀삥콜";
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
//        String sentence = "노 후보는 지난 4일 '탈 디제이' 기자회견과 관련해 \"구체적인 후속 프로그램은 가능한 것이 없다\"며 \"다만 한나라당이 특검제를 하자고 정치공세를 할 때 민주당이 기자회견 내용을 활용해 대응할 수 있을 것\"이라고 말했다."; //한다
//        String sentence = "동학의 접은 생태적 공생공동체(계·두레·친인척)이지만 도소(都所)와 포(包)로부터의 통문이 원심적으로 확산하되 접꾼 개개인의 영적 생명의 질적 성취를 통해서 무궁확산하는 기이한 소통공동체다."; //공생공동체
//        String sentence = "생태적 공생공동체(계·두레·친인척)이지만 도소(都所)와 포(包)로부터의 통문이 원심적으로 확산하되 접꾼 개개인의 영적 생명의 질적 성취를 통해서 무궁확산하는 기이한 소통공동체다."; //공생공동체

//        String sentence = "공생공동체"; //공생공동체
//        String sentence = "남자지갑"; //공생공동체
//        String sentence = "그 편이 나았으니까."; //편이
//        String sentence = "술래가 한 바퀴";
//        String sentence = "a.5kg 다우니운동화 남자지갑♧";
//        String sentence = "힐링프로젝트";
//        String sentence = "하늘을 나는 비행기";
//        String sentence = "소년은 캄캄한 방에 혼자 남자 덜컥 겁이 났다.";
//        String sentence = "거슬러 내려가셨다";
//        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "북한의 6차 핵실험으로 동북아 안보가 요동치는 가운데 6일 한·러 정상회담이 열렸다. 러시아는 '원유 공급 중단'이란 마지막 남은 대북(對北) 제재 실행의 열쇠를 쥔 나라다.";
//        String sentence = "BHC치킨을 먹자";
//        String sentence = "카무카무 올인원 마스터";
//        String sentence = "시타딘 온 버크 멜버른";
//        String sentence = "트래커";
//        String sentence = "MQ-76(128MB)녹음기/보이스레코더/MemoQ/대화+전화+계약녹음/녹취기/볼펜녹음기/보이스펜";
//        String sentence = "제때 치료받지 못해\n" + "마음의 병은 커져갑니다";
//        String sentence = "삭스앤삭스 여성용 발목밴딩 학생카바 쫀쫀핏 중목양말";
        String sentence = "슈에뜨룸 콧수욤 피치스킨 차렵이불"; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "쫀쫀핏"; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "콧수욤"; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "AL 헤어라인 윙 스포츠페달 튜닝킷"; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "\"스포츠페달,자동차페달,현대자동차용품,그랜저튜닝,싼타페튜닝,알루미늄페달,페달튜닝,페달교체,스포츠페달,닥쏘오토,닥쏘오토페달,자동차용품,차량용품,자동차튜닝,차량튜닝,튜닝용품\" \"자동차/오토바이용품^^^^^정비/장착/튜닝^^^^^차량용DIY용품^^^^^^^^^^\""; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "켁켁켁켁켁켁켁… 이것이 엄마의 마지막 말이었다."; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "(표 10-4 참조)."; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "야야야야야야야"; // 쫀쫀, 쫀핏 (unknown). connection null...
//        String sentence = "스타일리시한 루즈핏 아메리카반팔티 화이트 FREE - 지금 입기 딱좋아! 머시따 BEST 50종";
//        String sentence = "여성상의";
//        String sentence = "[나시공방] 4계절 365일 필수템 나시 필수템 이너웨어 단가라 망고 롱 플라워 브이넥 패션의류^^^^^여성상의^^^^^티셔츠^^^^^^^^^^";
//        String sentence = "들지않을";
//        String sentence = "다우니운동화 나이키운동화아디다스 ......남자지갑♧";
//        String sentence = "나이키운동화아디다스 ......남자지갑♧";
//        String sentence = "[아디다스]";
//        String sentence = "40일";
//        String sentence = "원로원과 협조하여 빈민 자녀의 부양 정책, 이탈리아의 도시, 농촌 회복 정책을 추진하였으며 대외적으로는 적극 정책을 펼쳤다.";
//        String sentence = "펼쳤다.";
//        String sentence = "아무리 추석과 겹쳤다기로서니 독일 통일의 축하가 곧 우리의 통일 의지를 자극하는 행사인 것을 모른대서야….";
//        String sentence = "자기 공명법에 의거한 경우는 자기 공명 분광술 (MRS)과 기능적 자기 공명 영상 (functional MRI; fMRI)이 있고, 동위원소 추적자에 의거한 방법은 단일 광자 방출 전산화 단층 촬영술 (single photon emission computed tomography; SPECT)과 양전자 방출 전산화 단층 촬영술 (positron emission tomo'graphy; PET)이 있다.)";
//        String sentence = "그러나 그 행동이 평이한 일상에서 너무 멀리 벗어나 있고 그런 행동을 낳은 '합리적인 배경'을 상상해 내기에 내 상상력이 너무 빈약함을 인정하겠지만, 그것이 불합리한 행동이라고 인정할 필요는 없을 것이다.";
//        String sentence = "쳉트리네오토바이.";
//        String sentence = "자기 공명법에 의거한 경우는";
//        String sentence = "그러나 그 행동이 평이한 일상에서 너무 멀리 벗어나 있고 그런 행동을 낳은 '합리적인 배경'을 상상해 내기에 내 상상력이 너무 빈약함을 인정하겠지만, 그것이 불합리한 행동이라고 인정할 필요는 없을 것이다.)";
//        String sentence = "무섭다.. 종국아.. 겁에 질렸잖아..";
//        String sentence = "내외국인";
//        String sentence = "단일안건 채택까지는 ";
//        String sentence = " ▶관련기사 3·15면";
//        String sentence = "부분 간질성 증후군 (Partial Epileptic Syndromes)";
//        String sentence = "이거야 원.";
//        String sentence = "3·1절";

//        String sentence = getStringFromTestCase();

//        List<String> nouns = daon.nouns(sentence);
//        System.out.println(nouns);

        long start = System.currentTimeMillis();
        EchoHandler handler = new EchoHandler();
        daon.analyzeWithHandler(sentence, handler);
        List<EojeolInfo> eojeolInfos = handler.getList();
        long end = System.currentTimeMillis();

        eojeolInfos.forEach(e -> {
            System.out.println(e.getSurface());
            e.getNodes().forEach(t -> {
                System.out.println(" '" + t.getSurface() + "' (" + t.getOffset() + ":" + (t.getOffset() + t.getLength()) + ")");
                for (Keyword k : t.getKeywords()) {
                    System.out.println("     " + k);
                }
            });
        });

        System.out.println((end - start) + "ms");

    }

    private String getStringFromTestCase() throws IOException {
        InputStream input = this.getClass().getResourceAsStream("testcase.txt");

        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line);
                textBuilder.append(System.lineSeparator());
            }
        }

        return textBuilder.toString();
    }


}
