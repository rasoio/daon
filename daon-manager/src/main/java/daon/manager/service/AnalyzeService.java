package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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


	public List<EojeolInfo> analyze(String text) throws IOException {

		List<EojeolInfo> terms = analyzer.analyzeText(text);

		//결과 obj 구성..

		return terms;
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