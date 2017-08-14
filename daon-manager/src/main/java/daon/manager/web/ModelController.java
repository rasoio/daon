package daon.manager.web;

import daon.manager.model.param.ModelParams;
import daon.manager.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/model")
public class ModelController {


	@Autowired
	private ModelService modelService;

	/**
	 * 모델 생성
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/make", method = RequestMethod.GET)
	public SparkAppHandle.State make() throws Exception {

		return modelService.make();
	}

	/**
	 * 모델 생성 상태
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/state", method = RequestMethod.GET)
	public SparkAppHandle.State state() throws Exception {

		return modelService.state();
	}

	/**
	 * 모델 다운로드
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public SearchResponse search(ModelParams modelParams) throws Exception {

		return modelService.search(modelParams);
	}

	/**
	 * 모델 다운로드
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(String seq) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		//파일 네이밍 정의 필요
		headers.add("Content-disposition", "attachment;filename=model_" + seq + ".dat");

		byte[] data = modelService.model(seq);
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(data.length)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);
	}

}