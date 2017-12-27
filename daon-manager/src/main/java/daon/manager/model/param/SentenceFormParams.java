package daon.manager.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mac on 2017. 4. 19..
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SentenceFormParams {

    private String id;
    private String index;
    private String sentence;
    private String eojeols;

}
