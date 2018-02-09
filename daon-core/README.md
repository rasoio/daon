### 모델 파일 적용

1. 기본 jar 파일내 모델 파일 사용
2. 외부 파일 참조 방법 JVM parameter 로 정의 예) -Ddaon.model.file="file path"
3. url 참조 JVM parameter 로 정의 예) -Ddaon.model.url="url"


### For analyzing

```java
import daon.core.Daon;
import daon.core.data.Eojeol;
import java.util.List;

public class TestExample {

    public static void main(String[] args) throws Exception {

        Daon daon = new Daon();

        List<Eojeol> eojeols = daon.analyze("아버지가방에들어가신다.");

        eojeols.forEach(e -> {
            System.out.println(e.getSurface());
            e.getMorphemes().forEach(m -> {
                System.out.println(" '" + m.getWord() + "' (" + m.getTag() + ")");
            });
        });
      
    }
}

```

### Output

```$xslt
아버지가방에들어가신다.
 '아버지' (NNG)
 '가' (JKS)
 '방' (NNG)
 '에' (JKB)
 '들어가' (VV)
 '시' (EP)
 'ㄴ다' (EF)
 '.' (SF)
```
