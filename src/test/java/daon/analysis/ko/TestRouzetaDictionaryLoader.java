package daon.analysis.ko;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import daon.analysis.ko.model.Keyword;

public class TestRouzetaDictionaryLoader {

	public static ObjectMapper om = new ObjectMapper();
	
//	@Ignore
	@Test
	public void load() throws Exception{
		Map<String,Keyword> dictionary = new HashMap<String,Keyword>();
		List<Keyword> dictionaries = new ArrayList<Keyword>();
		
		Map<String,List<String>> connectTagMatrix = new TreeMap<String,List<String>>();
		
		
		final InputStream in = TestSingleWordPhrase.class.getResourceAsStream("dict/reader/rouzenta.dic");
        try {
        	List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());
        	
        	String tag = "";
        	for(String line : lines){
        		if(line.startsWith("!") || StringUtils.isEmpty(line)){
        			continue;
        		}
        		
        		if(line.startsWith("LEXICON")){
        			String[] tags = line.split("[ ]+");
        			
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

        			String[] tags = line.split("[ ]+");
        			
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
        			
        			String[] dic = line.split("[ ]+");
        			
        			String keyword = replaceWord(dic[0]);
        			
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
        				
        				dictionaries.add(k);
        				
        				String key = k.getWord();
        				String t = k.getTag();
        				dictionary.put(key + t, k);
        				
        				/*
        				String key = k.getWord();
        				Keyword b = dictionary.get(key);
        				
        				if(b == null){
        					dictionary.put(key, k);
        				}else{

        					String irrRule = k.getIrrRule();
        					List<String> ts = k.getTags();
        					
        					if(irrRule != null){
        						b.setIrrRule(irrRule);
        					}
        					
        					for(String t : ts){
        						b.addTag(t);
        					}

        					dictionary.put(key, b);
        				}
        				*/
        			}
        		}
        		
        		
        		
        	}
        	
        } finally {
            IOUtils.closeQuietly(in);
        }
        
        for(Map.Entry<String, List<String>> entry : connectTagMatrix.entrySet()){
//        	System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        
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

//        dictionaries.stream().filter(k -> k.getTag().startsWith("v")).forEach(k -> System.out.println(k.getWord() + "	" + k.getTag()));
        
        /*
        System.out.println("group cont : " + dictionaries.stream().collect(Collectors.groupingBy(
              Keyword::getWord, 
              Collectors.mapping(Keyword::getTag, Collectors.toSet())
//              Collectors.counting()
        )).entrySet().stream().filter(map -> map.getValue().stream().filter(tag -> tag.startsWith("e")).count() > 0).count() );
        

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
        
        
        /*
        File dic = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/rouzenta_trans.dic");
		
		FileUtils.write(dic, "", Charset.defaultCharset(), false);
		
		for(Keyword word : dictionaries){
			String k = om.writeValueAsString(word);
			FileUtils.write(dic, k + IOUtils.LINE_SEPARATOR, Charset.defaultCharset(), true);
		}
		*/
        
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
					k.setTag(attr);
				}else{
					String wordTag = attr.substring(0, 2);
					k.setTag(wordTag);
					
					String word = attr.substring(2);
					Keyword nk = new Keyword();
					
					w += word;
					nk.setWord(word);
					list.add(nk);
				}
			}
		}
		
		r.setTag(curTag); // 복합어
		r.setWord(w);
		r.setSubWords(list);
		
		return r;
	}
}
