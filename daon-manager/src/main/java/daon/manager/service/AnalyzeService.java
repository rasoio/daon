package daon.manager.service;

import daon.core.Daon;
import daon.core.handler.EchoHandler;
import daon.core.handler.EojeolInfoHandler;
import daon.core.result.EojeolInfo;
import daon.manager.model.data.AnalyzedEojeol;
import daon.manager.model.param.TermParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class AnalyzeService {

	@Autowired
	private Daon daon;

	public List<AnalyzedEojeol> analyze(String text) throws IOException {

		if(StringUtils.isBlank(text)){
			return new ArrayList<>();
		}

        AnalyzeHandler handler = new AnalyzeHandler();
		daon.analyzeWithHandler(text, handler);

		return handler.getList();
	}

	class AnalyzeHandler implements EojeolInfoHandler {

        List<AnalyzedEojeol> list = new ArrayList<>();

        @Override
        public void handle(EojeolInfo eojeolInfo) {
            String surface = eojeolInfo.getSurface();

            List<TermParams> terms = eojeolInfo.getNodes().stream()
                    .map(node -> new TermParams(node.getSurface(), node.getKeywords()))
                    .collect(Collectors.toCollection(ArrayList::new));

            list.add(new AnalyzedEojeol(surface, terms));
        }

        @Override
        public List<AnalyzedEojeol> getList() {
            return list;
        }
    }

}