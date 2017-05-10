package daon.manager.model;

import daon.analysis.ko.model.MatchInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by mac on 2017. 4. 19..
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DictionaryParams {

    private String id;

    private String tag;

    private String keyword;

    private int from = 0;
    private int size = 10;

}
