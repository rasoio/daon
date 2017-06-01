package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.reader.ModelReader;
import daon.manager.config.DaonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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


	public List<Term> analyze(String text) throws IOException {

		List<Term> terms = analyzer.analyze(text);

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