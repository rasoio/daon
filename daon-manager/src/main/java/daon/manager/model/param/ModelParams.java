package daon.manager.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by mac on 2017. 4. 19..
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModelParams extends PageParams {

    private String id;

}
