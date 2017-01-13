package daon.analysis.ko;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

import daon.analysis.ko.util.CharTypeChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.analysis.util.RollingCharBuffer;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.util.Utils;
import daon.analysis.ko.dict.config.Config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {


    private Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public TestUtils() throws IOException {
    }

    @Test
	public void testNoCoda() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.endWithNoJongseong(new Keyword("간다", POSTag.un)));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("ㅁㄹ", POSTag.un)));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("가난", POSTag.un)));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("124", POSTag.un)));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("124ab", POSTag.un)));
		
	}
	
	@Test
	public void testCompound() throws JsonParseException, JsonMappingException, IOException{

		char test = '김';
		char[] c = Utils.decompose(test);
		
		Assert.assertTrue(test == Utils.compound(c[0], c[1], c[2]));
	}
	
	@Test
	public void testStartWith() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("간다", POSTag.un), new char[] {'ㄱ'}));
		Assert.assertFalse(Utils.startsWithChoseong(new Keyword("ㅂ니다", POSTag.un), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("바보", POSTag.un), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("으니", POSTag.un), new char[] {'ㅇ'}));
		
		Assert.assertTrue(Utils.startsWith(new Keyword("아", POSTag.un), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG));
		Assert.assertFalse(Utils.startsWith(new Keyword("아", POSTag.un), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.removeElement(Utils.JONGSEONG, new char[]{'\0'})));
		Assert.assertTrue(Utils.startsWith(new Keyword("답", POSTag.un), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}));
		Assert.assertTrue(Utils.startsWith(new Keyword("다", POSTag.un), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'\0'}));
	}
	
	@Test
	public void testEndWith() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.endWithChoseong(new Keyword("간다", POSTag.un), new char[] {'ㄷ'}));
		Assert.assertFalse(Utils.endWithChoseong(new Keyword("갑다ㄷ", POSTag.un), new char[] {'ㄷ'}));
		Assert.assertTrue(Utils.endWithChoseong(new Keyword("바보", POSTag.un), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.endWithChoseong(new Keyword("니은", POSTag.un), new char[] {'ㅇ'}));
		
		Assert.assertTrue(Utils.endWith(new Keyword("아", POSTag.un), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG));
		Assert.assertFalse(Utils.endWith(new Keyword("아", POSTag.un), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.removeElement(Utils.JONGSEONG, new char[]{'\0'})));
		Assert.assertTrue(Utils.endWith(new Keyword("답", POSTag.un), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}));
		
		Assert.assertTrue(Utils.endWith(new Keyword("다", POSTag.un), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'\0'}));
		
	}
	
	@Test
	public void testRemoveElement() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c = Utils.removeElement(Utils.JUNGSEONG, new char[]{'ㅏ', 'ㅓ'});
		
		Assert.assertTrue(Objects.deepEquals(c, new char[] {'ㅐ','ㅑ','ㅒ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'}));
		
	}
	
	@Test
	public void testGetCharAtDecompose() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c1 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), -1);
		char[] c2 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), -2);
		char[] c3 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), -3);
		

		char[] c4 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), 0);
		char[] c5 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), 1);
		char[] c6 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), 2);


		Assert.assertTrue(Objects.deepEquals(c1, new char[] {'ㄷ', 'ㅏ', '\0'}));
		Assert.assertTrue(Objects.deepEquals(c2, new char[] {'ㄱ', 'ㅏ', 'ㄴ'}));
		Assert.assertTrue(Objects.deepEquals(c3, new char[] {}));
		Assert.assertTrue(Objects.deepEquals(c4, new char[] {'ㄱ', 'ㅏ', 'ㄴ'}));
		Assert.assertTrue(Objects.deepEquals(c5, new char[] {'ㄷ', 'ㅏ', '\0'}));
		Assert.assertTrue(Objects.deepEquals(c6, new char[] {}));
		
	}
	
	@Test
	public void testMatch() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c1 = Utils.getCharAtDecompose(new Keyword("간달", POSTag.un), -1);
		char[] c2 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), -2);
		char[] c3 = Utils.getCharAtDecompose(new Keyword("간다", POSTag.un), -3);// empty
		

		Assert.assertFalse(Utils.isMatch(c1, new char[]{'ㄷ'}, new char[]{'ㅏ'}));
		
	}


//    File seojong = new File("/Users/mac/Downloads/sejong.txt");

//    String seojongTxt = FileUtils.readFileToString(seojong, Charset.defaultCharset());
    String seojongTxt = "테스트..";


	@Test
	public void testRead() throws IOException {

		StringReader input = new StringReader("가나다라마바사아자차카타파하ㄱㄴㄷ");

		StringBuilder document = new StringBuilder();
		char[] tmp = new char[3];
		int len;
		while ((len = input.read(tmp)) != -1) {
			document.append(new String(tmp, 0, len));
		}
		System.out.println(document.toString().toLowerCase());

	}

    @Test
    public void testRead1() throws IOException {


//        File seojong = new File("/Users/mac/Downloads/sejong.txt");

//        String seojongTxt = FileUtils.readFileToString(seojong, Charset.defaultCharset());

        int pos = 0;


        CharBuffer buffer = CharBuffer.allocate(12);

        StringReader input = new StringReader(seojongTxt);

        while (true) {

            if (input.read(buffer) == -1) {
                // End
                break;
            }

            buffer.flip();
            while(buffer.hasRemaining()){
                char ch =  buffer.get();

                logger.info("char : {}", ch);
            }
            buffer.clear();

        }



    }

	@Test
	public void testRead2() throws IOException {


		int pos = 0;
		int offset = 0;

		RollingCharBuffer buffer = new RollingCharBuffer();

//		StringReader input = new StringReader("시비와 분쟁이 있었을 때, 그 해결이 양편 다 옳은 것으로 되는 동시에 양편 다 옳지 않은 것이 되기도 한다.");
        StringReader input = new StringReader(seojongTxt);

        buffer.reset(input);

        StopWatch watch = new StopWatch();
        watch.start();

        while(pos > -1) {

            int length = 0;
            while (true) {
                int ch = buffer.get(pos);

                if (ch == -1) {
                    pos = -1;
                    // End
                    break;
                }

                if(length == 0){
                    offset = pos;
                }

                pos++;
                CharType charType = CharTypeChecker.charType(ch);


//                logger.info("char : {}, type : {}, pos : {}, length : {}", (char) ch, charType, pos, length);

                if (CharType.SPACE.equals(charType)) {
                    break;
                }

                length++;

//            System.out.println(buffer.get(pos, 1));
//            System.out.println((char) buffer.get(pos) +  " : " + charType);

            }

            try {

                if(length > 0) {
                    char[] chars = buffer.get(offset, length);
                }
//                logger.info("chars : {}, offset : {}, length : {}", chars, offset, length);
            }catch(AssertionError e){
                System.out.println("error");
                logger.info("pos : {}, offset : {}, length : {}", pos, offset, length);
            }

        }
        watch.stop();

        logger.info("elapsed time : {}", watch.getTime());

	}




    @Test
    public void testRead3() throws IOException {

        int pos = 0;
        int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;

        int IO_BUFFER_SIZE = 3;

        CharacterUtils charUtils = CharacterUtils.getInstance();
        CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

        StringReader input = new StringReader(seojongTxt);

        StopWatch watch = new StopWatch();
        watch.start();

        while(pos > -1){

            int length = 0;
            int start = -1; // this variable is always initialized
            int end = -1;
            char[] buffer = new char[10];

            while (true) {

                if (bufferIndex >= dataLen) {
                    offset += dataLen;
                    charUtils.fill(ioBuffer, input); // read supplementary char aware with CharacterUtils
                    if (ioBuffer.getLength() == 0) {
                        dataLen = 0; // so next offset += dataLen won't decrement offset
                        if (length > 0) {
                            break;
                        } else {
                            pos = -1;
                            //                        finalOffset = correctOffset(offset);
                            return;
                        }
                    }
                    dataLen = ioBuffer.getLength();
                    bufferIndex = 0;
                }

                final int ch = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
                final int charCount = Character.charCount(ch);
                bufferIndex += charCount;

                pos++;
                CharType charType = CharTypeChecker.charType(ch);

                if (!CharType.SPACE.equals(charType)) {               // if it's a token char
                    if (length == 0) {                // start of token
                        assert start == -1;
                        start = offset + bufferIndex - charCount;
                        end = start;
                    } else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
                        buffer = resizeBuffer(buffer, 2+length); // make sure a supplementary fits in the buffer
                    }
                    end += charCount;
                    length += Character.toChars(normalize(ch), buffer, length); // buffer it, normalized
                    if (length >= 4068) // buffer overflow! make sure to check for >= surrogate pair could break == test
                        break;
                } else if (length > 0)             // at non-Letter w/ chars
                    break;


            }

            Info info = new Info(buffer);

//            logger.info("chars : {}, pos : {}, ", info.chars);

        }
        watch.stop();

        logger.info("elapsed time : {}", watch.getTime());

    }

    protected int normalize(int c) {
        return c;
    }

    public final char[] resizeBuffer(char[] chars, int newSize) {
        char[] termBuffer = chars;
        if(chars.length < newSize){
            // Not big enough; create a new array with slight
            // over allocation and preserve content
            final char[] newCharBuffer = new char[ArrayUtil.oversize(newSize, Character.BYTES)];
            System.arraycopy(termBuffer, 0, newCharBuffer, 0, termBuffer.length);
            termBuffer = newCharBuffer;
        }
        return termBuffer;
    }


    class Info {
        int pos;
        String str;
        char[] chars;

        public Info(char[] chars) {
            this.chars = chars;
        }
    }
}
