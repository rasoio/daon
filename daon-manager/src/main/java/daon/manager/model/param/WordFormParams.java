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
public class WordFormParams {

    private String id;
    private String index;
    private String surface;
    private String morphemes;
    private int weight;

}
