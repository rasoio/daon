package daon.manager.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AliasIndices {

    private String alias;

    private List<String> options;
    private List<String> indices;

}
