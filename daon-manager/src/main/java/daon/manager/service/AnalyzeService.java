package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.model.Node;
import daon.manager.model.data.Eojeol;
import daon.manager.model.data.Term;
import daon.spark.PreProcess;
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
	private ModelService modelService;

	@Autowired
	private DaonAnalyzer analyzer;


	public List<Eojeol> analyze(String text) throws IOException {

		if(StringUtils.isBlank(text)){
			return new ArrayList<>();
		}

		List<EojeolInfo> eojeols = analyzer.analyzeText(text);

		//결과 obj 구성..
		List<Eojeol> results = eojeols.stream().map(e->{
			String surface = e.getSurface();

			List<Term> terms = e.getNodes().stream().map(node ->
				new Term(node.getSurface(), node.getKeywords())
			).collect(Collectors.toCollection(ArrayList::new));

			return new Eojeol(surface, terms);
		}).collect(Collectors.toCollection(ArrayList::new));


		return results;
	}

	public boolean reload() throws IOException {

		boolean isSuccess = false;
		try {

			ModelInfo modelInfo = modelService.modelInfo();

			analyzer.setModelInfo(modelInfo);

			isSuccess = true;
		}catch (IOException e){
			log.error("모델 reload error", e);
		}

		return isSuccess;
	}

}