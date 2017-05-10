package daon.analysis.ko.model;

import daon.analysis.ko.DaonAnalyzer2;
import daon.analysis.ko.DaonAnalyzer3;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.PairOutputs.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 3. 8..
 */
public class TestModel2 {

    private Logger logger = LoggerFactory.getLogger(TestModel2.class);

    private DaonAnalyzer2 daonAnalyzer2;




    @Before
    public void before() throws IOException {
         daonAnalyzer2 = new DaonAnalyzer2();
    }


    @Test
    public void test1() throws IOException, InterruptedException {


//        String test = "그가";
//        String test = "하늘을";
//        String test = "어디에 쓸 거냐거나 언제 갚을 수 있느냐거나 따위의, 돈을 빌려주는 사람이 으레 하게 마련인 질문 같은 것은 하지 않았다.";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
        String test = "아버지가방에들어가신다";
//        String test = "아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ";
//        String test = "1가A나다ABC라마바ABC";
//        String test = "사람이라는 느낌";
//        String test = "하늘을나는";
//        String test = "영광 굴비의 본디 이름은 정주 굴비다.";
//        String test = "우리나라는 대기 오염";
//        String test = "심쿵";
//        String test = "한나라당 조혜원님을";
//        String test = "도대체 어떻게 하라는거야?";
//        String test = "서울에서부산으로";

        List<CandidateTerm> terms = daonAnalyzer2.analyze(test);



        terms.forEach(System.out::println);
    }

    public List<CandidateTerm> read() throws IOException {
        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
        return daonAnalyzer2.analyze(test);
    }
}
