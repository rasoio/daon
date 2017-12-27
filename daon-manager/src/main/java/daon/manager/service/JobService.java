package daon.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import daon.manager.model.data.Message;
import daon.manager.model.data.Progress;
import daon.spark.model.MakeModel;
import daon.spark.tags.TagTrans;
import daon.spark.words.SentencesToWords;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ui.jobs.JobProgressListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class JobService {

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ObjectMapper mapper;

    private SparkSession sparkSession;

    private JobProgressListener sparkListener;

    private Future<Boolean> future;

    private StopWatch stopWatch;
    private String lastJobName;


    @Scheduled(fixedRate=1000)
    public void sendProgress() throws JsonProcessingException {

        Progress progress = progress();

        String json = mapper.writeValueAsString(progress);

        template.convertAndSend("/model/progress", json);
    }

    public void sendMessage(String type, String text) throws JsonProcessingException {
        Message message = new Message(type, text);

        String json = mapper.writeValueAsString(message);

        template.convertAndSend("/model/message", json);
    }


    public Progress execute(String jobName) throws IOException {

        if(future == null || future.isDone()) {
            Callable<Boolean> callable = () -> {

                try {
                    if("sentences_to_words".equals(jobName)){
                        sparkSession = SentencesToWords.getSparkSession();
                        addListeners(sparkSession.sparkContext());

                        SentencesToWords.execute(sparkSession);
                    }else if("tag_trans".equals(jobName)) {
                        sparkSession = TagTrans.getSparkSession();
                        addListeners(sparkSession.sparkContext());

                        TagTrans.execute(sparkSession);
                    }else{
                        sparkSession = MakeModel.getSparkSession();
                        addListeners(sparkSession.sparkContext());

                        MakeModel.execute(sparkSession);
                    }

                    sendMessage("END", jobName + " 완료되었습니다.");
                } catch (Exception e) {
                    log.error("모델 생성 에러", e);
                    sendMessage("END", jobName + " 실행 시 에러가 발생했습니다.");
                } finally {
                    sparkSession.close();
                }

                //완료 처리
                return true;
            };

            lastJobName = jobName;

            stopWatch = StopWatch.createStarted();
            future = executorService.submit(callable);

            sendMessage("START", jobName + " 실행을 시작했습니다.");
        }else{
            sendMessage("ING", "다른 Job이 수행중입니다.");
        }


        return progress();
    }


    private boolean isRunning(){
        if(future != null){
            boolean isDone = future.isDone();
            return !isDone;
        }else{
            return false;
        }
    }

    public void cancel() throws JsonProcessingException {
        if(sparkSession != null){
            sparkSession.sparkContext().cancelAllJobs();

            sendMessage("END", lastJobName + " Job 이 취소되었습니다.");
        }
    }


    private void addListeners(SparkContext context) {
        JobProgressListener pl = new JobProgressListener(context.getConf());
        context.addSparkListener(pl);

        sparkListener = pl;
    }

    public Progress progress() {

        boolean isRunning = isRunning();
        Progress progress = new Progress();
        progress.setRunning(isRunning);

        if(isRunning && sparkListener != null) {

            //listener 기준 completedJob

            int numCompletedJobs = sparkListener.numCompletedJobs();
            int numCompletedStages = sparkListener.numCompletedStages();
            int numFailedJobs = sparkListener.numFailedJobs();
            int numFailedStages = sparkListener.numFailedStages();

//            List<UIData.JobUIData> completedJobs = JavaConversions.bufferAsJavaList(sparkListener.completedJobs());

            progress.setNumCompletedJobs(numCompletedJobs);
            progress.setNumCompletedStages(numCompletedStages);
            progress.setNumFailedJobs(numFailedJobs);
            progress.setNumFailedStages(numFailedStages);

            stopWatch.split();
            long elapsedTime = stopWatch.getTime();
            progress.setElapsedTime(elapsedTime);

            progress.setJobName(lastJobName);

//            log.info("progress : {}", progress);
        }

        return progress;
    }

}