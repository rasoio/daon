package daon.manager.web;

import daon.manager.model.CorpusParams;
import daon.manager.service.CorpusService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/corpus")
public class CorpusController {


	@Autowired
	private CorpusService corpusService;

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public SearchResponse search(CorpusParams params) throws Exception {

		log.info("params : {}", params);

		return corpusService.search(params);
	}

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Map<String, Object> get(CorpusParams params) throws Exception {

		log.info("params : {}", params);

		return corpusService.get(params);
	}

}