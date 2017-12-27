package daon.manager.model.param;

import daon.core.result.Keyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TermParams {

    private String surface;

    private Keyword[] keywords;

}
