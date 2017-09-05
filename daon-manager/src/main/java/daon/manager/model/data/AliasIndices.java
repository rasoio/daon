package daon.manager.model.data;

import daon.analysis.ko.model.Keyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AliasIndices {

    private String alias;

    private List<Index> indices;

}
