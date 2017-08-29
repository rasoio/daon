package daon.manager.web;

import daon.analysis.ko.model.ModelInfo;
import daon.manager.model.data.AnalyzedEojeol;
import daon.manager.service.AnalyzeService;
import daon.manager.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/analyze")
public class AnalyzeController {


	@Autowired
	private AnalyzeService analyzeService;


	@Autowired
	private ModelService modelService;

	/**
	 * 텍스트 분석
	 * @param text
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/text", method = RequestMethod.POST)
	public List<AnalyzedEojeol> text(@RequestBody String text) throws Exception {

		log.info("keyword : {}", text);

		return analyzeService.analyze(text);
	}

	/**
	 * 텍스트 분석
	 * @param text
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/sentence", method = RequestMethod.GET)
	public List<AnalyzedEojeol> sentence(String text) throws Exception {

		log.info("keyword : {}", text);

		return analyzeService.analyze(text);
	}


	/**
	 * 텍스트 분석
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/reload", method = RequestMethod.GET)
	public boolean reload() throws Exception {

		ModelInfo modelInfo = modelService.defaultModelInfo();

		return analyzeService.reload(modelInfo);
	}

}