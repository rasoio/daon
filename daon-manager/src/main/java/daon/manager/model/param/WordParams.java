package daon.manager.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by mac on 2017. 4. 19..
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WordParams extends PageParams {

    private String id;
    private String index;

    private String surface;
    private String word;
    private String condition;
    private String[] indices;

}
