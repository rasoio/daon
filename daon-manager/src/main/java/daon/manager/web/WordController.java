package daon.manager.web;

import daon.manager.model.param.WordFormParams;
import daon.manager.model.param.WordParams;
import daon.manager.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/word")
public class WordController {


	@Autowired
	private WordService wordService;

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String search(@RequestBody WordParams params) throws Exception {

		log.info("params : {}, {}, {}", params, params.getFrom(), params.getSize());

		return wordService.search(params);
	}

	/**
	 * 말뭉치 검색
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Map<String, Object> get(WordParams params) throws Exception {

		log.info("params : {}", params);

		return wordService.get(params);
	}

	/**
	 * 말뭉치 저장
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@RequestBody WordFormParams params) throws Exception {

		log.info("params : {}", params);

		return wordService.upsert(params);
	}

	/**
	 * 말뭉치 삭제
	 * @param params
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(@RequestBody WordFormParams params) throws Exception {

		log.info("params : {}", params);

		return wordService.delete(params);
	}

	/**
	 * 말뭉치 저장
	 * @param uploadFile
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public List<String> upload(@RequestParam(value="uploadFile") MultipartFile uploadFile,
	                     @RequestParam(value="prefix") String prefix,
	                     @RequestParam(value="isAppend") boolean isAppend) throws Exception {

		log.info("uploadFile : {}, prefix : {}", uploadFile, prefix);

		List<String> errors = new ArrayList<>();

		try(InputStream input = uploadFile.getInputStream()){
			errors = wordService.upload(input, prefix, isAppend);

			errors.forEach(System.out::println);
		}

		return errors;
	}



}