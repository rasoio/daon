package daon.manager.model.data;

import daon.manager.model.param.TermParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AnalyzedEojeol {

    private String surface;

    private List<TermParams> terms;
}
