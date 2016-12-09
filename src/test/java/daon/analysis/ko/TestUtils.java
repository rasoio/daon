package daon.analysis.ko;

import java.io.IOException;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.util.Utils;

public class TestUtils {
	
	@Test
	public void testNoCoda() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.endWithNoJongseong(new Keyword("간다", "")));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("ㅁㄹ", "")));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("가난", "")));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("124", "")));
		Assert.assertFalse(Utils.endWithNoJongseong(new Keyword("124ab", "")));
		
	}
	
	@Test
	public void testCompound() throws JsonParseException, JsonMappingException, IOException{

		char test = '김';
		char[] c = Utils.decompose(test);
		
		Assert.assertTrue(test == Utils.compound(c[0], c[1], c[2]));
	}
	
	@Test
	public void testStartWith() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("간다", ""), new char[] {'ㄱ'}));
		Assert.assertFalse(Utils.startsWithChoseong(new Keyword("ㅂ니다", ""), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("바보", ""), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.startsWithChoseong(new Keyword("으니", ""), new char[] {'ㅇ'}));
		
		Assert.assertTrue(Utils.startsWith(new Keyword("아", ""), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG));
		Assert.assertFalse(Utils.startsWith(new Keyword("아", ""), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.removeElement(Utils.JONGSEONG, new char[]{'\0'})));
		Assert.assertTrue(Utils.startsWith(new Keyword("답", ""), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}));
		Assert.assertTrue(Utils.startsWith(new Keyword("다", ""), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'\0'}));
	}
	
	@Test
	public void testEndWith() throws JsonParseException, JsonMappingException, IOException{

		Assert.assertTrue(Utils.endWithChoseong(new Keyword("간다", ""), new char[] {'ㄷ'}));
		Assert.assertFalse(Utils.endWithChoseong(new Keyword("갑다ㄷ", ""), new char[] {'ㄷ'}));
		Assert.assertTrue(Utils.endWithChoseong(new Keyword("바보", ""), new char[] {'ㅂ'}));
		Assert.assertTrue(Utils.endWithChoseong(new Keyword("니은", ""), new char[] {'ㅇ'}));
		
		Assert.assertTrue(Utils.endWith(new Keyword("아", ""), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG));
		Assert.assertFalse(Utils.endWith(new Keyword("아", ""), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.removeElement(Utils.JONGSEONG, new char[]{'\0'})));
		Assert.assertTrue(Utils.endWith(new Keyword("답", ""), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}));
		
		Assert.assertTrue(Utils.endWith(new Keyword("다", ""), new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'\0'}));
		
	}
	
	@Test
	public void testRemoveElement() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c = Utils.removeElement(Utils.JUNGSEONG, new char[]{'ㅏ', 'ㅓ'});
		
		Assert.assertTrue(Objects.deepEquals(c, new char[] {'ㅐ','ㅑ','ㅒ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'}));
		
	}
	
	@Test
	public void testGetCharAtDecompose() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c1 = Utils.getCharAtDecompose(new Keyword("간다", ""), -1);
		char[] c2 = Utils.getCharAtDecompose(new Keyword("간다", ""), -2);
		char[] c3 = Utils.getCharAtDecompose(new Keyword("간다", ""), -3);
		

		char[] c4 = Utils.getCharAtDecompose(new Keyword("간다", ""), 0);
		char[] c5 = Utils.getCharAtDecompose(new Keyword("간다", ""), 1);
		char[] c6 = Utils.getCharAtDecompose(new Keyword("간다", ""), 2);


		Assert.assertTrue(Objects.deepEquals(c1, new char[] {'ㄷ', 'ㅏ', '\0'}));
		Assert.assertTrue(Objects.deepEquals(c2, new char[] {'ㄱ', 'ㅏ', 'ㄴ'}));
		Assert.assertTrue(Objects.deepEquals(c3, new char[] {}));
		Assert.assertTrue(Objects.deepEquals(c4, new char[] {'ㄱ', 'ㅏ', 'ㄴ'}));
		Assert.assertTrue(Objects.deepEquals(c5, new char[] {'ㄷ', 'ㅏ', '\0'}));
		Assert.assertTrue(Objects.deepEquals(c6, new char[] {}));
		
	}
	
	@Test
	public void testMatch() throws JsonParseException, JsonMappingException, IOException{
		
		char[] c1 = Utils.getCharAtDecompose(new Keyword("간달", ""), -1);
		char[] c2 = Utils.getCharAtDecompose(new Keyword("간다", ""), -2);
		char[] c3 = Utils.getCharAtDecompose(new Keyword("간다", ""), -3);// empty
		

		Assert.assertTrue(Utils.isMatch(c1, new char[]{'ㄷ'}, new char[]{'ㅏ'}));
		
	}
	
}
