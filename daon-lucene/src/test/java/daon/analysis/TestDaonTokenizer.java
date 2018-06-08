package daon.analysis;

import daon.core.config.POSTag;
import daon.core.result.Keyword;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.TestRuleLimitSysouts;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@TestRuleLimitSysouts.Limit(bytes = 1000000)
public class TestDaonTokenizer extends BaseTokenStreamTestCase {

    private Logger logger = LoggerFactory.getLogger(TestDaonTokenizer.class);

    private Analyzer analyzer;
//    private String input = "하루아침에 되나?";
//    private String input = "우리나라에서 만세 피치키친 " + line() + " ee " + line();
    private String input = "당신이 주목해야 할 소식들!" + line();

    @Before
    public void before() throws IOException {
        analyzer = new DaonAnalyzer("query");

//        input = getStringFromTestCase();
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


    public void testProducts() throws FileNotFoundException {

        DaonAnalyzer analyzer = new DaonAnalyzer("index");

        File f = new File("/Users/mac/Downloads/deals.txt");
        FileInputStream fileInputStream = new FileInputStream(f);

        int num = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {

                String dealname = line;

                try (TokenStream ts = analyzer.tokenStream("bogus", dealname)){
                    num++;
                    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
                    OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
                    TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);
                    PositionIncrementAttribute posIncAtt = ts.addAttribute(PositionIncrementAttribute.class);

                    int cnt = 0;
                    ts.reset();
                    int before_end = 0;
                    int last_start = 0;
                    while (ts.incrementToken()) {
                        cnt++;

                        String term = termAtt.toString();
                        int start = offsetAtt.startOffset();
                        int end = offsetAtt.endOffset();
//                        (startOffset < invertState.lastStartOffset || endOffset < startOffset)
                        if (start < last_start || end < start || start < 0) {
                            logger.info("position error dealname : {}, start : {}, last_start : {}, end : {}", dealname, termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset(), typeAtt.type(), posIncAtt.getPositionIncrement());
                        }
                        if (term.isEmpty()) {
                            logger.info("empty term : {}, ({},{}), type : {}, posInc : {}, dealname : {}", termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset(), typeAtt.type(), posIncAtt.getPositionIncrement(), dealname);
                        }

                        last_start = start;
                    }

                    if (cnt == 0) {
                        logger.info("deal_name : {}", dealname);
                    }


                    if (num % 1000000 == 0) {
                        logger.info("process : {}", num);
                    }

                    ts.end();
                }catch (Exception e){
                    logger.error("error dealname : {} num : {}", line, num, e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    class Deal{
        private long DID;
        private String DEALNAME;

        public long getDID() {
            return DID;
        }

        public void setDID(long DID) {
            this.DID = DID;
        }

        public String getDEALNAME() {
            return DEALNAME;
        }

        public void setDEALNAME(String DEALNAME) {
            this.DEALNAME = DEALNAME;
        }
    }


    public void testIndexOf(){

        //keyword's
//        "흘러들어가는","흐르/VV 어/EC 들어가/VV 는/ETM","흘러 - 흐르/VV 어/EC||흘러들어가 - 흐르/VV 어/EC 들어가/VV||흘러들어가는 - 흐르/VV 어/EC 들어가/VV 는/ETM||들어가는 - 들어가/VV 는/ETM"
//        "흘러들어가","흐르/VV 어/EC 들어가/VV 아/EC","흘러들어가 - 흐르/VV 어/EC 들어가/VV 아/EC"
//        "누군가가","누구/NP 이/VCP ㄴ가/EC 가/JKS","누군가 - 누구/NP 이/VCP ㄴ가/EC||누군가가 - 누구/NP 이/VCP ㄴ가/EC 가/JKS"
//        "늦을까봐","늦/VV 을까/EC 보/VV 아/EC","늦을까 - 늦/VV 을까/EC||늦을까봐 - 늦/VV 을까/EC 보/VV 아/EC||을까봐 - 을까/EC 보/VV 아/EC||봐 - 보/VV 아/EC"
//        "가야지.","가/VV 아야지/EF ./SF","가야지
//        Keyword[] keywords = new Keyword[] {
//            new Keyword("질리", POSTag.VV),
//            new Keyword("었", POSTag.EP),
//            new Keyword("다", POSTag.EF),
//            new Keyword(".", POSTag.SP),
//        };
//        Keyword[] keywords = new Keyword[] {
//            new Keyword("가", POSTag.VV),
//            new Keyword("아야지", POSTag.EF),
//            new Keyword(".", POSTag.SP),
//        };

//        Keyword[] keywords = new Keyword[] {
//            new Keyword("자연", POSTag.NNG),
//            new Keyword("스럽", POSTag.XSA),
//            new Keyword("ㄴ", POSTag.ETM),
//            new Keyword("자연", POSTag.NNG),
//        };
//
//        String surface = "스러운";
////        String surface = "스러운자연";
////        String surface = "자연스러운자연";

//        Keyword[] keywords = new Keyword[] {
//                new Keyword("어떡하", POSTag.VV),
//                new Keyword("어", POSTag.EF),
//                new Keyword("?", POSTag.SP),
//        };

        Keyword[] keywords = new Keyword[] {
                new Keyword("불러내", POSTag.VV),
                new Keyword("어", POSTag.EF),
                new Keyword("가", POSTag.EF),
                new Keyword("잖어", POSTag.EF),
        };

//        String surface = "어떡해?";
        String surface = "불러내가잖어";
//        String surface = "스러운";
//        String surface = "자연스러운자연";
//        String surface = "질렸다.";
//        String surface = "가야지.";

        int length = keywords.length;

        int from = 0;
        int offset = 0;
        //불규칙 조합
        for (int i = 0; i <length; i++) {
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int termLength = term.length();

            boolean isMatch = isMatch(surface, term, from);

            if(isMatch){
                System.out.println(term + " : " + keyword.getTag());


                offset += termLength;
                from += termLength;
            }else{

                String partial = surface.substring(from);
//                System.out.println(partial + " : " + term);

//                int move = mergeIrrTerm(partial, keywords, i, length);
                Token token = mergeIrrTerm(partial, keywords, i, offset, length);

                if(token != null) {
                    termLength = token.getLength();
                    from += termLength;
                    int move = token.getKeywords().size();

                    if (move > 0) move -= 1;
                    i += move;

                    System.out.println(token.getTerm() + " : " + token.getType());
                }
            }


        }

//        String surface = "자연스러운";
//        System.out.println(surface.indexOf("스럽"));
    }


    private boolean isMatch(String surface, String term, int from){
        int surfaceLength = surface.length();
        int termLength = term.length();

        if(surfaceLength < from + termLength){
            return false;
        }

        return term.equals(surface.substring(from, from + termLength));
    }


    private boolean isBackwardMatch(String surface, String term, int end){
        if(end < 0) return false;

        return term.equals(surface.substring(end));
    }


    private Token mergeIrrTerm(String surface, Keyword[] keywords, int startIdx, int offset, int length){

        List<Keyword> irrKeywords = new ArrayList<>();

        //backward search
        int endIndex = surface.length();
        int end = 0;
        for(int i = length-1; i>= startIdx; i--){
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int termLength = term.length();

            endIndex -= termLength;

            boolean isMatch = isBackwardMatch(surface, term, endIndex);

            if(isMatch){
                surface = surface.substring(0, endIndex);
            }else{
                end = i;
                break;
            }
        }

        String mergeSurface = surface;

        if(mergeSurface.isEmpty()){
            return null;
        }

        for (int i = startIdx; i <= end; i++) {
            Keyword keyword = keywords[i];
            irrKeywords.add(keyword);
        }

        int termLength = mergeSurface.length();

        String types = irrKeywords.stream().map(k->k.getTag().getName()).collect(Collectors.joining("+"));

        Token token = new Token(mergeSurface, offset, termLength, types);
        token.setKeywords(irrKeywords);

        return token;
    }

    private int mergeIrrTerm(String surface, Keyword[] keywords, int startIdx, int length){

        List<String> type = new ArrayList<>();

        int end = surface.length();
        int move = 0;
        int from = 0;
        for (int i = startIdx; i < length; i++) {
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int termLength = term.length();

            from += termLength/2;
            int idx = surface.indexOf(term, from);

            if(idx > -1){
                end = idx;
                break;
            }else{
                type.add(keyword.getTag().getName());
            }

            move++;
        }

        if(move > 0){
            move -= 1;
        }

        String extractSurface = surface.substring(0, end);

        System.out.println(extractSurface + " : " + type);

        return move;
    }


    public void testLongText() throws IOException {

        TokenStream ts = analyzer.tokenStream("bogus", "[패션플러스 / 비씨비지]BCBG포스팅스커트(B8A1S203)");
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);
        PositionIncrementAttribute posIncAtt = ts.addAttribute(PositionIncrementAttribute.class);

        ts.reset();
        while (ts.incrementToken()) {
            logger.info("term : {}, ({},{}), type : {}, posInc : {}", termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset(), typeAtt.type(), posIncAtt.getPositionIncrement());
        }
        ts.end();
        ts.close();
    }

    public void testAnalyze() throws IOException {
        String input = "우리나라 만세 " + line() + " ee " + line();

        assertAnalyzesTo(analyzer, input,
                new String[] { "우리나라", "만세", "ee" },
                new int[] { 0, 5, 11},
                new int[] { 4, 7, 13}
        );
    }

    public void testIrregularOffset() throws IOException {

        assertAnalyzesTo(analyzer, "자연스러운",
                new String[] { "자연", "스러운" },
                new int[] { 0, 2},
                new int[] { 2, 5}
        );
    }

    public String line(){
//        return System.lineSeparator();
        return "\r\n";
    }
}
