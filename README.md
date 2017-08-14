# daon
[![Build Status](https://travis-ci.org/rasoio/daon.svg?branch=master)](https://travis-ci.org/rasoio/daon)

말뭉치 기반의 한글 형태소 분석기입니다.




말뭉치 :
 
[Rouzeta](https://shleekr.github.io/)에서 사용 된 수정된 세종 코퍼스입니다.

[https://ithub.korean.go.kr/user/member/memberPdsReferenceManager.do](https://ithub.korean.go.kr/user/member/memberPdsReferenceManager.do)



참고 문헌 :

[1] 신준철, 옥철영 (2012). 기분석 부분 어절 사전을 활용한 한국어 형태소 분석기. 정보과학회논문지 : 소프트웨어 및 응용, 39(5), 415-424.

[2] [http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html](http://blog.mikemccandless.com/2010/12/using-finite-state-transducers-in.html)

[3] [https://shleekr.github.io/](https://shleekr.github.io/)

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
