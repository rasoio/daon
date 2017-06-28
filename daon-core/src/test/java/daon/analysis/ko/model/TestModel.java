package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.reader.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 3. 8..
 */
public class TestModel {

    private Logger logger = LoggerFactory.getLogger(TestModel.class);

    private DaonAnalyzer daonAnalyzer;




    @Before
    public void before() throws IOException {

        ModelInfo modelInfo = ModelReader.create().filePath("/Users/mac/work/corpus/model/model6.dat").load();

        daonAnalyzer = new DaonAnalyzer(modelInfo);

    }


    @Test
    public void test1() throws IOException, InterruptedException {


//        String test = "그가";
//        String test = "하늘을";
//        String test = "어디에 쓸 거냐거나 언제 갚을 수 있느냐거나 따위의, 돈을 빌려주는 사람이 으레 하게 마련인 질문 같은 것은 하지 않았다.";
        String test = "남자지갑";
//        String test = "우리나라만세";
//        String test = "열세다.";
//        String test = "3일";
//        String test = "불길이 모든 것들을 다 태우고 나서 점차 사그라들어 재만 남자, 형제들은 절 안에 있는 큰방에 모여 묵묵히 함께 절 음식을 먹었다.";
//        String test = "있다.";
//        String test = "바란다.";
//        String test = "그리고 우리들은 마크 로브슨 감독의 <페이톤 플레이스(Peyton Place)>에서 다이안 바시와 러스 탬블린의 어색한 첫 키스를 불안해 했고, <피서지에서 생긴일>을 놓고 샌드라 디와 함께 걱정했다.";
//        String test = "그래서였을까.";
//        String test = "나는 그 천식 같은 소리에서 박자도 가려낼 수가 없었지만 한번 배그파이프에 익숙해지면 북처럼 쉽게 박자를 분별할 수 있는 모양이다.";
//        String test = "그래서 율곡은 선조에게 올린 만언봉사라는 글에서 옛 조상의 법은 바꿀 수 없다는 통념에 반대하고 것이다.";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "하늘을 나는 비행기";
//        String test = "국토부는 시장 상황과 맞지 않는 일률적인 규제를 탄력적으로 적용할 수 있도록 법 개정을 추진하는 것이라고 설명하지만, 투기 세력에 기대는 부동산 부양책이라는 비판이 일고 있다.";
//        String test = "집 앞에서 고추를 말리던 이숙희(가명·75) 할머니의 얼굴에는 웃음기가 없었다. \"나라가 취로사업이라도 만들어주지 않으면 일이 없어. 섬이라서 어디 다른 데 나가서 일할 수도 없고.\" 가난에 익숙해진 연평도 사람들은 '정당'과 '은혜'라는 말을 즐겨 썼다.";
//        String test = "포털의 '속초' 연관 검색어로 '포켓몬 고'가 올랐고, 속초시청이 관광객의 편의를 위해 예전에 만들었던 무료 와이파이존 지도는 순식간에 인기 게시물이 됐다.";
//        String test = "미국 국방부가 미국 미사일방어망(MD)의 핵심 무기체계인 사드(THAAD)를 한국에 배치하는 방안을 검토하고 있다고 <월스트리트 저널>이 28일(현지시각) 보도했다.";
//        String test = "멋진 나는 밥만있어도 함께 먹는다.";
//        String test = "먹었겠어?";
//        String test = "DSS와";
//        String test = "백패커스";
//        String test = "a.5kg";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "\"네놈들한테죽는게분할뿐이다.";
//        String test = "네놈들한테 죽는 게 분할 뿐이다.";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
//        String test = "아버지가방에들어가신다";
//        String test = "소매달린";
//        String test = "집중단속하기로";
//        String test = "우리나라만세";
//        String test = "OS";
//        String test = "아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ";
//        String test = "1가A나다ABC라마바ABC";
//        String test = "사람이라는 느낌";
//        String test = "하늘을나는";
//        String test = "영광 굴비의 본디 이름은 정주 굴비다.";
//        String test = "우리나라는 대기 오염";
//        String test = "심쿵";
//        String test = "한나라당 조혜원님을";
//        String test = "도대체 어떻게 하라는거야?";
//        String test = "서울에서부산으로";eligible

        List<Term> terms = daonAnalyzer.analyze(test);
//        List<EojeolInfo> eojeols = daonAnalyzer.analyzeText(test);

        terms.forEach(System.out::println);
//        eojeols.forEach(System.out::println);

        terms.forEach(t->{
            for(Keyword k : t.getKeywords()) {
                System.out.println(k.getSeq() + ", " + k);
            }
        });

    }

    public List<Term> read() throws IOException {
        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
        return daonAnalyzer.analyze(test);
    }
}
