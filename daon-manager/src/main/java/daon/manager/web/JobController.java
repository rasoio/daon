package daon.manager.web;

import daon.manager.model.data.Progress;
import daon.manager.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mac on 2017. 3. 8..
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/job")
public class JobController {

	@Autowired
	private JobService jobService;


	/**
	 * 모델 생성
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/execute", method = RequestMethod.GET)
	public Progress execute(String jobName) throws Exception {

		return jobService.execute(jobName);
	}

	/**
	 * 모델 적용
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/cancel", method = RequestMethod.GET)
	public boolean cancel() throws Exception {

		jobService.cancel();

		return true;
	}

	/**
	 * 모델 생성 상태
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/progress", method = RequestMethod.GET)
	public Progress progress() throws Exception {

		return jobService.progress();
	}

}