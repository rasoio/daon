package daon.analysis.ko;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestKKMDictionaryLoader {

	private Map<String,WordInfo> words = new TreeMap<String,WordInfo>();

	private List<WordInfo> nounList = new ArrayList<WordInfo>();
	private List<WordInfo> verbList = new ArrayList<WordInfo>();
	private List<WordInfo> adverbList = new ArrayList<WordInfo>();
	private List<WordInfo> josaList = new ArrayList<WordInfo>();
	private List<WordInfo> eomiList = new ArrayList<WordInfo>();
	
	@Ignore
	@Test
	public void load() throws Exception{
		
		
//		N("Noun"), // 체언
//		V("Verb"), // 용언
//		M("Adverb"), // 수식언, 독립언
//		J("Josa"), // 관계언
//		E("Eomi") // 용언 어미
//		
		
		File home_dir = new File("/Users/mac/Downloads/kkm/dic"); 
		
//		Iterator<File> files = FileUtils.iterateFiles(home_dir, new WildcardFileFilter("*.dic"), TrueFileFilter.TRUE);
		Collection<File> files = FileUtils.listFiles(home_dir, new String[]{"dic"}, false);
		for(File f : files){
			if(f.getName().startsWith("raw")){
				continue;
			}
			
			List<String> lines = FileUtils.readLines(f);
			
			for(String line : lines){
				if(StringUtils.isBlank(line)){
					continue;
				}
				
				String[] sp = line.split("[/]");
				
				if(sp.length == 1){
					System.out.println(line);					
				}
				
				String word = sp[0];
				String type = sp[1];
				
				if(type.contains(";")){
					type = type.substring(0, type.indexOf(";"));
				}
				

				WordInfo w = new WordInfo(0, word, type, 100);
				
				addWordInfo(w);
				
				
//				if(words.get(word) != null){
//					System.out.println(word + " : " + words.get(word));					
//				}
				
//				words.put(word, f.getAbsolutePath());
				
				
//				System.out.println(line);
			}
			
			System.out.println(f.getAbsolutePath());
		}
		
		
		File rawdic = new File(home_dir, "raw.dic");
		

		List<String> lines = FileUtils.readLines(rawdic);
		
		for(String line : lines){
			if(StringUtils.isBlank(line) || line.startsWith("//")){
				continue;
			}
			
//			System.out.println(line);
			
			String[] sp = line.split("[:]");
			
			String word = sp[0];
			String type = sp[1];
			
//			System.out.println(type);

			Pattern p = Pattern.compile("\\[(.*)\\]");
			Matcher m = p.matcher(type);
			
			if(m.find()){
				String text = m.group(1);
				
				
				String[] datas = text.split("[+]");
				

				for(String data : datas){

					String[] subsp = data.split("[/]");

//					System.out.println("data!" + ToStringBuilder.reflectionToString(subsp));
					if(subsp.length == 2){

						String subWord = subsp[0];
						String subType = subsp[1];

						
						WordInfo w = new WordInfo(0, subWord, subType, 100);
						
						addWordInfo(w);
					}
					
				}
			}
			
		}
		

		File dic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/kkm.dic");
		
		FileUtils.write(dic, "", Charset.defaultCharset(), false);
		
		for(String word : words.keySet()){
			WordInfo w = words.get(word);
			
			FileUtils.write(dic, w.toString() + IOUtils.LINE_SEPARATOR, Charset.defaultCharset(), true);
		}

		/*
//		N("Noun"), // 체언
//		V("Verb"), // 용언
//		M("Adverb"), // 수식언, 독립언
//		J("Josa"), // 관계언
//		E("Eomi") // 용언 어미
//		
		System.out.println("noun : " + nounList.size());
		System.out.println("verb : " + verbList.size());
		System.out.println("adverb : " + adverbList.size());
		System.out.println("josa : " + josaList.size());
		System.out.println("eomi : " + eomiList.size());
		
		
		File nounDic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/noun.dic");
		FileUtils.writeLines(nounDic, nounList, false);

		File verbDic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/verb.dic");
		FileUtils.writeLines(verbDic, verbList, false);
		
		File adverbDic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/adverb.dic");
		FileUtils.writeLines(adverbDic, adverbList, false);
		
		File josaDic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/josa.dic");
		FileUtils.writeLines(josaDic, josaList, false);
		
		File eomiDic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko/eomi.dic");
		FileUtils.writeLines(eomiDic, eomiList, false);
		*/
	}


	private void addWordInfo(WordInfo w) {
		
		String word = w.getWord();
		String type = w.getType();
		
		if(StringUtils.isBlank(word)){
			return;
		}
		
		WordInfo wd = words.get(word);
		
		if(wd != null){
			wd.addType(type);
		}else{
			words.put(word, w);
		}
		
//		if(w.getType().startsWith("N")){
//			nounList.add(w);
//		}else if(w.getType().startsWith("V")){
//			verbList.add(w);
//		}else if(w.getType().startsWith("M") || w.getType().startsWith("I")){
//			adverbList.add(w);
//		}else if(w.getType().startsWith("J")){
//			josaList.add(w);
//		}else if(w.getType().startsWith("E") || w.getType().startsWith("X")){
//			eomiList.add(w);
//		}
	}
	
	@Test
	public void makeSeq() throws IOException{
		
		File rawdic = new File("/Users/mac/work/workspace/daon/src/test/resources/daon/analysis/ko", "kkm.dic");
		

		List<String> lines = FileUtils.readLines(rawdic);
		List<Keyword> keywords = new ArrayList<Keyword>();
		
		long seq = 0;
		for(String line : lines){
			if(StringUtils.isBlank(line) || line.startsWith("//")){
				continue;
			}
			
			Keyword word = om.reader().forType(Keyword.class).readValue(line);
			word.setSeq(seq);
			seq++;
			
			keywords.add(word);
//			WordInfo word = om.readValue(line, WordInfo.class);
			
		}
		
		
		
							 
		File dic = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/kkm2.dic");
		
		FileUtils.write(dic, "", Charset.defaultCharset(), false);
		
		for(Keyword word : keywords){
			String w = om.writeValueAsString(word);

			System.out.println(w);
			FileUtils.write(dic, w + IOUtils.LINE_SEPARATOR, Charset.defaultCharset(), true);
		}
	}
	
	public static ObjectMapper om = new ObjectMapper();

	public class WordInfo {
		private long seq;
		private String word;
		private String type;
		private Set<String> types;
		private int tf;

		
		
		public WordInfo() {
			super();
		}

		public WordInfo(long seq, String word, String type, int tf){
			this.seq = seq;
			this.word = word;
			this.type = type;
			this.types = new TreeSet<String>();
			this.types.add(type);
			this.tf = tf;
		}
		
		public WordInfo(String word, Set<String> attr, int tf){
			this.word = word;
			this.types = attr;
			this.tf = tf;
		}
		
		public void setTf(int tf) {
			this.tf = tf;
		}

		public long getSeq() {
			return seq;
		}

		public void setSeq(long seq) {
			this.seq = seq;
		}

		public String getWord() {
			return word;
		}
		public void setWord(String word) {
			this.word = word;
		}
		
		public void setAttr(Set<String> types) {
			this.types = types;
		}
		
		public Set<String> getAttr() {
			return types;
		}
		
		public long getTf() {
			return 100;
		}
		
		@JsonIgnore
		public String getType() {
			return type;
		}

		@JsonIgnore
		public void setType(String type) {
			this.type = type;
		}

		
		public void addType(String type) {
			this.types.add(type);
		}

		@Override
		public String toString() {
			
			try {
				return om.writeValueAsString(this);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			om.writeValue(g, this);
			return "";
			
//			return "{\"word\":\"" + word + "\",\"attr\":" + types + ",\"tf\":100}";
		}
		
		
	}
}
