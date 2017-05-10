package daon.manager.web;

import daon.manager.model.CorpusParams;
import daon.manager.model.DictionaryParams;
import daon.manager.service.CorpusService;
import daon.manager.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/dictionary")
public class DictionaryController {


	@Autowired
	private DictionaryService dictionaryService;

	/**
	 * 사전 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public SearchResponse search(DictionaryParams params) throws Exception {

		log.info("params : {}", params);

		return dictionaryService.search(params);
	}

	/**
	 * 사전 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Map<String, Object> get(DictionaryParams params) throws Exception {

		log.info("params : {}", params);

		return dictionaryService.get(params);
	}

}