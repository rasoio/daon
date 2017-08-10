# daon
[![Build Status](https://travis-ci.org/rasoio/daon.svg?branch=master)](https://travis-ci.org/rasoio/daon)

말뭉치 기반의 한글 형태소 분석기입니다.


말뭉치 : 
[https://ithub.korean.go.kr/user/member/memberPdsReferenceManager.do](https://ithub.korean.go.kr/user/member/memberPdsReferenceManager.do)

참고 문헌 :

[1] [http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html](http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html)

[2] [https://shleekr.github.io/](https://shleekr.github.io/)

[3] 신준철, "말뭉치 기반 부분 어절 기분석 사전의 구축과 형태소 분석"
, 한국정보과학회언어공학연구회 2011년도 제23회 한글 및 한국어 정보처리 학술대회, 2011년, pp.67-72

[4] [https://bitbucket.org/eunjeon/](https://bitbucket.org/eunjeon/)

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
