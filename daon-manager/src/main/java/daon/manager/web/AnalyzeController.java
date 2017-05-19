package daon.manager.web;

import daon.analysis.ko.model.Term;
import daon.manager.service.AnalyzeService;
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

	/**
	 * 텍스트 분석
	 * @param text
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/text", method = RequestMethod.GET)
	public List<Term> text(String text) throws Exception {

		log.info("keyword : {}", text);

		return analyzeService.analyze(text);
	}

}