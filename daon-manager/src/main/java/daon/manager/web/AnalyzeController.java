package daon.manager.web;

import daon.analysis.ko.model.EojeolInfo;
import daon.manager.service.AnalyzeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

	/**
	 * 텍스트 분석
	 * @param text
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/text", method = RequestMethod.GET)
	public List<EojeolInfo> text(String text) throws Exception {

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

		return analyzeService.reload();
	}

}