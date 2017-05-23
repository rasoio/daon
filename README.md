# daon
[![Build Status](https://travis-ci.org/rasoio/daon.svg?branch=master)](https://travis-ci.org/rasoio/daon)

한글 형태소 분석기입니다.

lucene 기반의 dictionaryFst 소스를 활용해서 작업하고 있습니다.

# Usage

### For analyzing

[DaonAnalyzerTest.java](https://github.com/rasoio/daon/blob/master/src/main/java/daon/analysis/ko/main/DaonAnalyzerExample.java)


```bash

gradlew -f daon-manager/

```

```java
import daon.analysis.ko.model.*;
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.reader.ModelReader;

public class DaonAnalyzerTest {

    public static void main(String[] args) throws Exception {

        ModelInfo modelInfo = ModelReader.create().load();
        
        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(modelInfo);

        List<Term> terms = analyzer.analyze("아버지가방에들어가신다");

        System.out.println("################ results #################");
        
        terms.forEach(System.out::println);

    }
}

```
