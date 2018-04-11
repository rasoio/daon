package daon.core;

import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.handler.MorphemeHandler;
import daon.core.util.Utils;
import org.junit.Test;

import java.util.List;

public class TestExample {

    @Test
    public void test() throws Exception {
        //초기화
        Daon daon = new Daon();
        MorphemeHandler handler = new MorphemeHandler();
        handler.setExcludeBit(Utils.makeTagBit(new String[]{"J","E","SN"})); // 제외 품사 지정

        //분석
        daon.analyzeWithHandler("나이키아디다스신발", handler);

        //분석 결과 사용
        List<Morpheme> list = handler.getList();

        list.forEach(m -> {
            System.out.println(" '" + m.getWord() + "' (" + m.getTag() + ")");
        });

        List<Eojeol> eojeols = daon.analyze("아버지가방에들어가신다.");

        eojeols.forEach(e -> {
            System.out.println(e.getSurface());
            e.getMorphemes().forEach(m -> {
                System.out.println(" '" + m.getWord() + "' (" + m.getTag() + ")");
            });
        });
    }
}
