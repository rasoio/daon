# daon
한글 형태소 분석기입니다.

lucene 기반의 소스를 활용해서 작업하고 있습니다.

[TestDictionary.java](https://github.com/rasoio/daon/blob/master/src/test/java/daon/analysis/ko/TestDictionary.java) 작업중. JUnit 구동 가능. 

# Usage

### For analyzing

```java
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.config.Config.DicType;
import daon.analysis.ko.dict.reader.FileDictionaryReader;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;

public class DaonAnalyzerTest {

    public static void main(String[] args) throws Exception {

        Dictionary dictionary = DictionaryBuilder.create()
                              .setDicType(DicType.KKM)
                              .setFileName("kkm.dic")
                              .setReader(new FileDictionaryReader())
                              .build();

        DaonAnalyzer analyzer = new DaonAnalyzer();
        analyzer.setDictionary(dictionary);

        ResultTerms results = analyzer.analyze("아버지가방에들어가신다");
        
        System.out.println("################ results #################");
        for(Term t : results.getResults()){
          System.out.println(t);
        }
    
    }
}

```
