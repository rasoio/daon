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
public class PageParams {

    private int from = 0;
    private int size = 10;

}
