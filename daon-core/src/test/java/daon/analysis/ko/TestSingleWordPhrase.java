package daon.analysis.ko;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;
import org.junit.Test;

import daon.analysis.ko.util.Utils;

public class TestSingleWordPhrase {


    @Test
    public void makeNounPart() throws Exception {

//		final InputStream in = TestSingleWordPhrase.class.getResourceAsStream("splithangul.txt");
//		
//		List<String> keywords = IOUtils.readLines(in, Charsets.toCharset(Charset.defaultCharset().name()));
//		
//		Collections.sort(keywords);
//		
//		for(String keyword : keywords){
//			System.out.println(keyword);
//		}


//		Automaton a = Automata.makeString("사과를");

        //체언 목록
        Automaton a = Automata.makeString("ㅅㅏㄱㅗㅏ");

        //조사 'ㄹ'
        RegExp reg = new RegExp("(ㄹㅡㄹ|_ㄹ)");
        Automaton b = reg.toAutomaton();

        Automaton c = Operations.concatenate(a, b);

//		MinimizationOperations.minimize(a, maxDeterminizedStates)
        CharacterRunAutomaton r = new CharacterRunAutomaton(c);

//		System.out.println(r.toString());

//		String str = c.toDot();

//		System.out.println(str);
        System.out.println(r.run("ㅅㅏㄱㅗㅏ_ㄹ"));
//		System.out.println(b.getNumStates());
//		System.out.println(b.getNumTransitions());

        List<String> nouns = new ArrayList<String>();
        nouns.add("사과");
        nouns.add("사람");

        for (String noun : nouns) {
            System.out.println(noun);


//			if()
            System.out.println(noun);


        }

        System.out.println(decode("\\u54840"));


//		! 은/는
//		! Filter0 : 사과는
//		define FilterPT0 ~$[ FILLC NounStringSet ㅇ ㅡ %_ㄴ %/pt ] ;
//
//		! Filter1 : 사람은
//		define FilterPT1 ~$[ [Coda - FILLC] NounStringSet ㄴ ㅡ %_ㄴ %/pt ] ;


//		System.out.println(StringUtils.getLevenshteinDistance("깨닫", "깨달아"));
//		System.out.println(StringUtils.getLevenshteinDistance("사괄", "사과"));
//		System.out.println(StringUtils.getLevenshteinDistance("ㄴ가", "온가"));


    }

    @Test
    public void test1() {
        String tt = "가";
        System.out.println("===> " + tt.substring(0, tt.length() - 1));

        Automaton at = new RegExp("(([ㄷ|ㄲ|ㄱ|ㄴ|ㅁ|ㄸ]ㅏ)|ㄹ[ㅏ|ㅗ])ㅂ").toAutomaton();
        Automaton a = new RegExp("[ㄱ | ㄴ | ㄷ | ㄹ | ㅁ | ㅂ | ㅅ | ㅇ | ㅈ | ㅊ | ㅋ | ㅌ | ㅍ | ㅎ | ㄲ | ㄸ | ㅃ | ㅆ | ㅉ]").toAutomaton();
        Automaton d = new RegExp("[ㄱ | ㄴ ]").toAutomaton();

//		((ㄴ ㅣ) | (ㄹ) | (ㅁ) | (ㅅ ㅣ) | (ㅇ ㅗ) ).*
        Automaton eu = new RegExp("(ㄴㅣ|ㄹ|ㅁ|ㅅㅣ|ㅇㅗ).*").toAutomaton();

        a = Operations.minus(a, d, Operations.DEFAULT_MAX_DETERMINIZED_STATES);
        Automaton b = new RegExp("ㅡ").toAutomaton();

        Automaton c = Operations.concatenate(a, b);
        CharacterRunAutomaton r = new CharacterRunAutomaton(c);
        CharacterRunAutomaton tr = new CharacterRunAutomaton(at);
        CharacterRunAutomaton eur = new CharacterRunAutomaton(eu);

        char[] tc = Utils.decompose('를');
        char[] t = Utils.decompose('그');

        System.out.println(tc + " : " + tc.length);
//		System.out.println(r.run(t, 0, t.length));
//		System.out.println(tr.run(tc, 0, tc.length));
        System.out.println(eur.run(tc, 0, tc.length));
        System.out.println(Operations.run(eu, new String(tc)));

//		System.out.println(at.toDot());
    }


    @Test
    public void test2() {
//		((ㄴ ㅣ) | (ㄹ) | (ㅁ) | (ㅅ ㅣ) | (ㅇ ㅗ) ).*


        Automaton eu = new RegExp("(ㄴㅣ|ㄹ|ㅁ|ㅅㅣ|ㅇㅗ).+").toAutomaton();

        Automaton eu2 = new RegExp("[ㄴ|ㄹ|ㅁ]").toAutomaton();

        CharacterRunAutomaton eur = new CharacterRunAutomaton(eu);

        char c = '를';

        char[] tc = Utils.decompose(c);

        System.out.println("test2 check : " + c + ", " + eur.run(tc, 0, tc.length));

    }


    @Test
    public void test3() {
        Automaton dropL1 = new RegExp("(ㄴ|ㄹ|ㅂ)").toAutomaton();
        Automaton dropL2 = new RegExp("(ㄴ|ㅅㅣ|ㅇㅗ|ㅅㅔ).*").toAutomaton();

        CharacterRunAutomaton dropLr = new CharacterRunAutomaton(Operations.union(dropL1, dropL2));

        char c = 'ㄴ';

        char[] tc = Utils.decompose(c);

        System.out.println(ToStringBuilder.reflectionToString(tc));
        System.out.println("dropLr check : " + c + ", " + dropLr.run(tc, 0, tc.length));

    }


    @Test
    public void test4() {
        Automaton a = new RegExp("((ㄱ|ㅈ|ㄴ)ㅏ|ㅂㅗ)" + Utils.EMPTY_JONGSEONG + "|ㅇㅣㅆ").toAutomaton();

        CharacterRunAutomaton cr = new CharacterRunAutomaton(a);

        char c = '있';

        char[] tc = Utils.decompose(c);


        System.out.println(ToStringBuilder.reflectionToString(tc));
        System.out.println("test4 check : " + c + ", " + cr.run(tc, 0, tc.length));

    }


    @Test
    public void test5() {

//		define PhnOnsetNLM	[ ㄴ | ㄹ | ㅁ ] ;
//		define PhnCodaNLM	[ %_ㄴ | %_ㄹ | %_ㅁ ] ;
//		define ChangeH		%_ㅎ -> 0 || _ %/irrh [%/vj | %/vi] 
//										[ ㅇ ㅡ [ PhnCodaNLM | FILLC PhnOnsetNLM ] | %_ㅂ ] ;

        Automaton a = new RegExp("ㅇㅡ(ㄴ|ㄹ|ㅁ)|ㅂ").toAutomaton();
        Automaton b = new RegExp("ㅇㅡ" + Utils.EMPTY_JONGSEONG + "(ㄴ|ㄹ|ㅁ)").toAutomaton();


        CharacterRunAutomaton cr = new CharacterRunAutomaton(Operations.union(a, b));

        char c = '음';

//		char[] tc = Utils.decompose(c);
        char[] tc = new char[]{'ㅇ', 'ㅡ', '\0', 'ㄴ'};

        System.out.println(ToStringBuilder.reflectionToString(tc));
        System.out.println("test5 check : " + c + ", " + cr.run(tc, 0, tc.length));

    }

    public static String decode(String unicode) throws Exception {
        StringBuffer str = new StringBuffer();

        char ch = 0;
        for (int i = unicode.indexOf("\\u"); i > -1; i = unicode.indexOf("\\u")) {
            ch = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
            str.append(unicode.substring(0, i));
            str.append(String.valueOf(ch));
            unicode = unicode.substring(i + 6);
        }
        str.append(unicode);

        return str.toString();
    }


}
