package daon.manager.web;

import daon.manager.model.param.SentenceFormParams;
import daon.manager.model.param.TagParams;
import daon.manager.model.param.WordParams;
import daon.manager.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/tag")
public class TagController {


	@Autowired
	private TagService tagService;

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String search(@RequestBody TagParams params) throws Exception {

        log.info("params : {}, {}, {}", params, params.getFrom(), params.getSize());

		return tagService.search(params);
	}

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Map<String, Object> get(TagParams params) throws Exception {

		log.info("params : {}", params);

		return tagService.get(params);
	}

}