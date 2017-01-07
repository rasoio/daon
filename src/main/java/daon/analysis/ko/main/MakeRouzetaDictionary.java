package daon.analysis.ko.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.TagConnection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.*;

public class MakeRouzetaDictionary {

	public static ObjectMapper om = new ObjectMapper();

	public void load() throws Exception{
		Map<String,Keyword> dictionary = new HashMap<String,Keyword>();
		List<Keyword> dictionaries = new ArrayList<Keyword>();
		
		Map<String,List<String>> connectTagMatrix = new TreeMap<String,List<String>>();
		
		
		Map<String,Float> wordProb = new HashMap<String,Float>();
		
		File csv = new File("/Users/mac/Downloads/tf.csv");
		
//		FileUtils.write(csv, word + "	" + keyword + "	" + cnt + System.lineSeparator(), "UTF-8", true);

		List<String> tfLines = IOUtils.readLines(new FileInputStream(csv), Charset.defaultCharset());
		
		//최대값
//		int max = tfLines.stream().mapToInt(line -> NumberUtils.toInt(line.split("\t")[2])).max().getAsInt();
//		System.out.println(max);
		
		//최대값(X), 총 사전 단어 노출 수
		int totalWordCnt = 22231026;
		
//		forEach(line -> {
//			System.out.println(line[2]);
//		});
		
		for(String line : tfLines){
			
			String[] v = line.split("\t");
			
//			System.out.println(v[0] + ", " + v[1] + ", " + v[2]);
			
			int tf = NumberUtils.toInt(v[2]);
			
			float prob = 100;
			
			if(tf > 0){
				prob = (float) -Math.log((float) tf / (float) totalWordCnt);
			}
//			float normTf = (float) tf / max;
			
//			System.out.printf(tf + " / "  + max + " = %.10f" + System.lineSeparator(), normTf);
			wordProb.put(v[0], prob);
		}
		
//		tfs.entrySet().stream().forEach(e -> {
//			System.out.println("str : " + e.getKey() + ", tf : " + String.format("%.10f", e.getValue()));
//		});
		
//		System.exit(0);
		
		
		//유니크 카운트 구하기
//		tfInfos.stream().collect(Collectors.groupingBy(
//              Keyword::getWord, 
//              Collectors.mapping(Keyword::getTag, Collectors.toSet())
//        )).entrySet().stream().sorted(Comparator.nullsFirst(new Comparator<Map.Entry<String,Set<String>>>() {
//  			@Override
//  			public int compare(Map.Entry<String,Set<String>> left, Map.Entry<String,Set<String>> right) {
//  				return left.getKey().compareTo(right.getKey());
//  			}
//  		})).filter(map -> map.getValue().stream().filter(tag -> tag.startsWith("e")).count() > 0).forEach(k -> {
//	        Keyword keyword = new Keyword(k.getKey(), "ef"); 
//	        try {
//				System.out.println(om.writeValueAsString(keyword));
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        });

		File rouzenta = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/rouzenta.dic");
		FileInputStream in = new FileInputStream(rouzenta);
        try {
        	List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());
        	
        	String tag = "";
        	for(String line : lines){
        		if(line.startsWith("!") || StringUtils.isEmpty(line)){
        			continue;
        		}
        		
        		if(line.startsWith("LEXICON")){
        			String[] tags = line.split("\\s+");
        			
        			tag = tags[1];
        			
        			if(!tag.contains("Lexicon")){
        			
        				tag = tag.replace("Next", "");
        				connectTagMatrix.put(tag, new ArrayList<String>());
        			
        			}else {
        				tag = tag.replace("Lexicon", "");
        			}
//        			LEXICON Root
//        		       ncLexicon ; ! 보통명사
//        			
//        			
        		}else if(line.startsWith(" ")){

        			String[] tags = line.split("\\s+");
        			
        			String nTag = tags[1];
        			
        			if(nTag.contains("Lexicon")){
        				nTag = nTag.replace("Lexicon", "");
	        			
	        			List<String> connectTags = connectTagMatrix.get(tag);
	
//	        			System.out.println(tag + " : " + nTag);
	        			
	        			connectTags.add(nTag);
        			
        			}
        			
        		}else{
        			
//        			if(line.startsWith("짓")){
//        				System.out.println(line);
//        			}
        			
        			String[] dic = line.split("\\s+");
        			
        			String rawKeyword = dic[0];
        			
        			float prob = wordProb.get(rawKeyword);
        			
        			System.out.println(rawKeyword + "	" + prob);
        			
        			String keyword = replaceWord(rawKeyword);
        			
        			String[] attrs = keyword.split("[/]+");
        			
        			Keyword k = null;
        			if(attrs.length == 2){
        				String word = replaceWord(attrs[0]);
        				String wordTag = attrs[1];
        				
        				if(wordTag.startsWith("s") && word.startsWith("%") && word.length() > 1){
        					word = word.replaceFirst("%", "");
        				}else if(wordTag.startsWith("s") && "%".equals(word)){
        					word = "/";
        				}
        				
        				k = new Keyword(word, wordTag);
        			}else if(attrs.length == 3){
//        				System.out.println(keyword);
        				
        				String etc = attrs[1];
        				
        				if(etc.startsWith("irr")){
        					String word = replaceWord(attrs[0]);
        					String irrRule = attrs[1];
            				String wordTag = attrs[2];
            				
            				k = new Keyword(word, wordTag);
            				k.setIrrRule(irrRule);
//            				System.out.println(word + " : " + irr + " : " + wordTag);

        				}else{
        					
        					k = parse(attrs, tag);

        				}
        				
        			}else if(attrs.length >= 4){
        				
        				k = parse(attrs, tag);
        				
        			}else{
        				//ignore
//        				System.out.println("empty!!!! ==> " + keyword);
        			}

        			if(k != null){
        				
        				k.setProb(prob);
        				
        				dictionaries.add(k);
        				
        				String key = k.getWord();
        				String t = k.getTag().name();
        				dictionary.put(key + t, k);
        				
        			}
        		}
        		
        		
        		
        	}
        	
        } finally {
            IOUtils.closeQuietly(in);
        }
        
        /*
        List<TagConnection> tags = new ArrayList<TagConnection>();
        
        for(Map.Entry<String, List<String>> entry : connectTagMatrix.entrySet()){
        	
        	TagConnection connection = new TagConnection();
        	connection.setTag(entry.getKey());
        	connection.setTags(entry.getValue());
        	
        	System.out.println(entry.getKey() + " : " + entry.getValue());
        	
        	tags.add(connection);
        }
        
        File tagDic = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/connect_matrix.dic");
		
		FileUtils.write(tagDic, "", Charset.defaultCharset(), false);
		
		for(TagConnection tag : tags){
			String t = om.writeValueAsString(tag);
			FileUtils.write(tagDic, t + IOUtils.LINE_SEPARATOR, Charset.defaultCharset(), true);
		}
        
        System.out.println(dictionaries.size());
        */

        /*
        dictionaries.stream().filter(k -> {
        	if(k.getTf() < 2 && k.getTag().startsWith("e")){
            	return true;
        	}else{
        		return false;
        	}
        	
        }).forEach(k -> {
      	  System.out.println(k.getWord() + "	" + k.getTag() + "	" + k.getTf());
        });
        
        System.out.println("totalCnt : " + dictionaries.stream().filter(k -> {
        	if(k.getTf() < 2 && k.getTag().startsWith("e")){
            	return true;
        	}else{
        		return false;
        	}
        	
        }).count());
        */
        
        
        /*
        //유니크 카운트 구하기
        dictionaries.stream().collect(Collectors.groupingBy(
              Keyword::getWord, 
              Collectors.mapping(Keyword::getTag, Collectors.toSet())
//              Collectors.counting()
        )).entrySet().stream().sorted(Comparator.nullsFirst(new Comparator<Map.Entry<String,Set<String>>>() {
  			@Override
  			public int compare(Map.Entry<String,Set<String>> left, Map.Entry<String,Set<String>> right) {
  				
  				return left.getKey().compareTo(right.getKey());
  			}
  		})).filter(map -> map.getValue().stream().filter(tag -> tag.startsWith("e")).count() > 0).
        forEach(k -> {
	        Keyword keyword = new Keyword(k.getKey(), "ef"); 
	        try {
				System.out.println(om.writeValueAsString(keyword));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//	        System.out.println(k.getKey() + "	" + k.getValue())
        });
        */

//        dictionaries.stream().filter(k -> k.getTag().startsWith("v")).forEach(k -> System.out.println(k.getWord() + "	" + k.getTag()));
        
        /*
        System.out.println("group cont : " + dictionaries.stream().collect(Collectors.groupingBy(
              Keyword::getWord, 
              Collectors.mapping(Keyword::getTag, Collectors.toSet())
//              Collectors.counting()
        )).entrySet().stream().filter(map -> map.getValue().stream().filter(tag -> tag.startsWith("e")).count() > 0).count() );
        
        
        dictionaries.stream().collect(Collectors.groupingBy(
                Keyword::getWord, 
                Collectors.mapping(Keyword::getTag, Collectors.toSet())
//                Collectors.counting()
          )).entrySet().stream().filter(map -> map.getValue().stream().filter(tag -> tag.startsWith("e")).count() > 0).forEach(e -> {
        	  System.out.println(e.getKey() + "	" + e.getValue());  
          });
        

        System.out.println("raw cont : " + dictionaries.stream().filter(k -> k.getTag().startsWith("e")).count());
        */
        
//        for(int i=0,len = dictionaries.size();i<len; i++){
//        	Keyword k = dictionaries.get(i);
//
//        	System.out.println(om.writeValueAsString(k));
//        }
        
        //오름차순 정렬
        Collections.sort(dictionaries, Comparator.nullsFirst(new Comparator<Keyword>() {
  			@Override
  			public int compare(Keyword left, Keyword right) {
  				
  				return left.getWord().compareTo(right.getWord());
  			}
  		}));
        
        //seq 채번
        for(int i=0,len = dictionaries.size();i<len; i++){
        	Keyword k = dictionaries.get(i);
        	k.setSeq(i+1);
        	
        }
        
        //subword 채번
        for(int i=0,len = dictionaries.size();i<len; i++){
        	Keyword k = dictionaries.get(i);
        	
        	if(k.getSubWords() != null){
        		for(Keyword sk : k.getSubWords()){
        			String key = sk.getWord() + sk.getTag();
        			
        			Keyword tk = dictionary.get(key);
        			if(tk != null){
        				
        				sk.setSeq(tk.getSeq());
        			}
        		}
        	}
        }
        
        //write file
        File dic = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/rouzenta_trans.dic");
		
		FileUtils.write(dic, "", Charset.defaultCharset(), false);
		
		for(Keyword word : dictionaries){
			String k = om.writeValueAsString(word);
			
			FileUtils.write(dic, k + IOUtils.LINE_SEPARATOR, Charset.defaultCharset(), true);
		}
        
        /*
        for(Map.Entry<String, Keyword> entry : dictionary.entrySet()){
//        	System.out.println(entry.getKey() + " : " + entry.getValue());
        	
        	Keyword k = entry.getValue();
        	if("irrb".equals(k.getIrrRule())){
        		System.out.println(k);
        	}
        	
        }
        */
        
	}
	
	private String replaceWord(String word){
		
		word = word.toLowerCase();
		
		if("%_/so".equals(word) || "%_".equals(word)){
			return word;
		}
		
		if("//".equals(word)){
			return word.replaceFirst("/", "");
		}
		
		return word.replaceAll("%_", "");
	}

	private Keyword parse(String[] attrs, String curTag) {
		List<Keyword> list = new ArrayList<Keyword>();
		Keyword r = new Keyword();
		
		String w = "";
		
		for(int i=0; i < attrs.length; i++){
			String attr = replaceWord(attrs[i]);
			
			if(i == 0){
				w += attr;
				
				Keyword k = new Keyword();
				k.setWord(attr);
				list.add(k);
			}else{
				
				Keyword k = list.get(list.size() - 1);
				
				if(attr.length() == 2){
					k.setTag(Config.POSTag.valueOf(attr));
				}else{
					String wordTag = attr.substring(0, 2);
					k.setTag(Config.POSTag.valueOf(wordTag));
					
					String word = attr.substring(2);
					Keyword nk = new Keyword();
					
					w += word;
					nk.setWord(word);
					list.add(nk);
				}
			}
		}
		
		r.setTag(Config.POSTag.valueOf(curTag)); // 복합어
		r.setWord(w);
		r.setSubWords(list);
		
		return r;
	}
	
	class Tf {
		private String word;
		private int tf;
	}

	public static void main(String[] args) throws Exception {


		MakeRouzetaDictionary makeRouzetaDictionary = new MakeRouzetaDictionary();
		makeRouzetaDictionary.load();
	}
}
