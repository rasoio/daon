### 모델 파일 적용

1. 기본 jar 파일내 모델 파일 사용
2. 외부 파일 참조 방법 JVM parameter 로 정의 예) -Ddaon.model.file="file path"
3. url 참조 JVM parameter 로 정의 예) -Ddaon.model.url="url"


### For analyzing

```java
import daon.analysis.ko.model.*;
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.reader.ModelReader;

public class DaonAnalyzerTest {

    public static void main(String[] args) throws Exception {

        ModelInfo modelInfo = ModelReader.create().load();
        
        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(modelInfo);

        List<EojeolInfo> eojeolInfos = daonAnalyzer.analyzeText("아버지가방에들어가셨다");
        
        eojeolInfos.forEach(e->{
            System.out.println(e.getEojeol());
            e.getNodes().forEach(t->{
                System.out.println(" '" + t.getSurface() + "' (" + t.getOffset() + ":" + (t.getOffset() + t.getLength()) + ")");
                for(Keyword k : t.getKeywords()) {
                    System.out.println("     " + k);
                }
            });
        });
      
    }
}

```

### Output

```$xslt
아버지가방에들어가셨다
 '아버지가방에들어가' (0:9)
     (seq : 141283, word : 아버지, tag : NNG, freq : 6877)
     (seq : 1410, word : 가, tag : JKS, freq : 202188)
     (seq : 88573, word : 방, tag : NNG, freq : 3806)
     (seq : 149987, word : 에, tag : JKB, freq : 357140)
     (seq : 63092, word : 들어가, tag : VV, freq : 6710)
 '셨다' (9:11)
     (seq : 132061, word : 시, tag : EP, freq : 17253)
     (seq : 149905, word : 었, tag : EP, freq : 340073)
     (seq : 48875, word : 다, tag : EF, freq : 446105)
```
