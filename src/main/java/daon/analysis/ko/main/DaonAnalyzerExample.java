package daon.analysis.ko.main;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.connect.ConnectMatrix;
import daon.analysis.ko.dict.connect.ConnectMatrixBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.Term;

/**
 * Created by mac on 2017. 1. 5..
 */
public class DaonAnalyzerExample {

    public static void main(String[] args) throws Exception {

        ConnectMatrix connectMatrix = ConnectMatrixBuilder.create()
                .setFileName("connect_matrix.dic")
                .setReader(new FileReader<TagConnection>())
                .setValueType(TagConnection.class).build();
        Dictionary dic = DictionaryBuilder.create()
                .setFileName("rouzenta_trans.dic")
                .setReader(new FileReader<Keyword>())
                .setValueType(Keyword.class)
                .setConnectMatrix(connectMatrix).build();

        DaonAnalyzer analyzer = new DaonAnalyzer(dic);

        ResultTerms results = analyzer.analyze("아버지가방에들어가신다");

        System.out.println("################ results #################");
        for(Term t : results.getResults()){
            System.out.println(t);
        }

    }
}
