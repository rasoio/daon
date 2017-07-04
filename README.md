# daon
[![Build Status](https://travis-ci.org/rasoio/daon.svg?branch=master)](https://travis-ci.org/rasoio/daon)

한글 형태소 분석기입니다.


참고 문헌 :
 
[1] [https://shleekr.github.io/](https://shleekr.github.io/)

[2] [신준철, "말뭉치 기반 부분 어절 기분석 사전의 구축과 형태소 분석"](http://society.kisti.re.kr/sv/SV_svpsbs03V.do?method=view)
, 한국정보과학회언어공학연구회 2011년도 제23회 한글 및 한국어 정보처리 학술대회, 2011년, pp.67-72

[3] [http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html](http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html)

# Usage

### For analyzing

```java
import daon.analysis.ko.model.*;
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.reader.ModelReader;

public class DaonAnalyzerTest {

    public static void main(String[] args) throws Exception {

        ModelInfo modelInfo = ModelReader.create().load();
        
        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(modelInfo);

        List<EojeolInfo> eojeolInfos = daonAnalyzer.analyzeText("아버지가방에들어가신다");
        
        eojeolInfos.forEach(e->{
            System.out.println(e.getEojeol());
            e.getTerms().forEach(t->{
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
아버지가방에들어가셨다.
 '아버지가' (0:4)
     (seq : 149149, word : 아버지, tag : NNG, freq : 7641)
     (seq : 1474, word : 가, tag : JKS, freq : 224602)
 '방에' (4:6)
     (seq : 93390, word : 방, tag : NNG, freq : 4231)
     (seq : 158331, word : 에, tag : JKB, freq : 396921)
 '들어가셨다' (6:11)
     (seq : 66514, word : 들어가, tag : VV, freq : 7459)
     (seq : 139341, word : 시, tag : EP, freq : 19104)
     (seq : 158247, word : 었, tag : EP, freq : 377902)
     (seq : 51512, word : 다, tag : EF, freq : 495669)
 '.' (11:12)
     (seq : 24, word : ., tag : SF, freq : 784744)
```
