package daon.manager.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by mac on 2017. 4. 19..
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TagFormParams {

    private String id;

    private String position;
    private String tag1;
    private String tag2;
    private int cost;

}
