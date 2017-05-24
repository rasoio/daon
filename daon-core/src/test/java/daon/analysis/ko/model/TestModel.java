package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.reader.ModelReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mac on 2017. 3. 8..
 */
public class TestModel {

    private Logger logger = LoggerFactory.getLogger(TestModel.class);

    private DaonAnalyzer daonAnalyzer;




    @Before
    public void before() throws IOException {

        ModelInfo modelInfo = ModelReader.create().load();

        daonAnalyzer = new DaonAnalyzer(modelInfo);

    }


    @Test
    public void test1() throws IOException, InterruptedException {


//        String test = "그가";
//        String test = "하늘을";
//        String test = "어디에 쓸 거냐거나 언제 갚을 수 있느냐거나 따위의, 돈을 빌려주는 사람이 으레 하게 마련인 질문 같은 것은 하지 않았다.";
//        String test = "남자지갑";
        String test = "열세다.";
//        String test = "3일";
//        String test = "불길이 모든 것들을 다 태우고 나서 점차 사그라들어 재만 남자, 형제들은 절 안에 있는 큰방에 모여 묵묵히 함께 절 음식을 먹었다.";
//        String test = "있다.";
//        String test = "바란다.";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "하늘을나는 비행기";
//        String test = "백패커스";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "\"네놈들한테죽는게분할뿐이다.";
//        String test = "\"네놈들한테 죽는 게 분할 뿐이다.";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
//        String test = "아버지가방에들어가신다";
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



        terms.forEach(System.out::println);

        int[] data = new int[]{1,2,3,4,5,6,7,8,9,10};
        Arrays.sort(data);
        int idx = Arrays.binarySearch(data, 11);

        System.out.println(idx);

        Thread.sleep(100000);
    }

    public List<Term> read() throws IOException {
        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
        return daonAnalyzer.analyze(test);
    }
}
