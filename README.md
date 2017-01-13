# daon
[![Build Status](https://travis-ci.org/rasoio/daon.svg?branch=master)](https://travis-ci.org/rasoio/daon)

한글 형태소 분석기입니다.

lucene 기반의 fst 소스를 활용해서 작업하고 있습니다.

# Usage

### For analyzing

[DaonAnalyzerTest.java](https://github.com/rasoio/daon/blob/master/src/main/java/daon/analysis/ko/main/DaonAnalyzerExample.java)

```java
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.connect.ConnectionCosts;
import daon.analysis.ko.dict.connect.ConnectionCostsBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.Term;

public class DaonAnalyzerTest {

    public static void main(String[] args) throws Exception {

        ConnectMatrix connectionCosts = ConnectMatrixBuilder.create()
                .setFileName("connect_matrix.dic")
                .setReader(new FileReader<TagConnection>())
                .setValueType(TagConnection.class).build();
        Dictionary dic = DictionaryBuilder.create()
                .setFileName("rouzenta_trans.dic")
                .setReader(new FileReader<Keyword>())
                .setValueType(Keyword.class)
                .setConnectMatrix(connectionCosts).build();

        DaonAnalyzer analyzer = new DaonAnalyzer(dic);

        ResultTerms results = analyzer.analyze("아버지가방에들어가신다");

        System.out.println("################ results #################");
        for(Term t : results.getResults()){
            System.out.println(t);
        }

    }
}

```
