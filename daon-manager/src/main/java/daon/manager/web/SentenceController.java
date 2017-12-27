package daon.manager.web;

import daon.manager.model.param.SentenceFormParams;
import daon.manager.model.param.SentenceParams;
import daon.manager.service.SentenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/sentence")
public class SentenceController {


	@Autowired
	private SentenceService sentenceService;

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String search(@RequestBody SentenceParams params) throws Exception {

		log.info("params : {}, {}, {}", params, params.getFrom(), params.getSize());

		return sentenceService.search(params);
	}

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Map<String, Object> get(SentenceParams params) throws Exception {

		log.info("params : {}", params);

		return sentenceService.get(params);
	}

	/**
	 * 말뭉치 저장
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@RequestBody SentenceFormParams params) throws Exception {

		log.info("params : {}", params);

		return sentenceService.upsert(params);
	}

}