package daon.manager.service;

import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.reader.ModelReader;
import daon.manager.model.data.Progress;
import daon.manager.model.param.ModelParams;
import daon.spark.MakeModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.spark.SparkContext;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ui.jobs.JobProgressListener;
import org.apache.spark.ui.jobs.UIData;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.String.valueOf;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class ModelService {

    @Value("${spark.home}")
    private String home;

    @Value("${spark.appResource}")
    private String appResource;

    @Value("${spark.master}")
    private String master;

    @Autowired
    private TransportClient client;

    @Autowired
    private ExecutorService executorService;

    private static String INDEX = "models";
    private static String TYPE = "model";

    private SparkSession sparkSession;

    private JobProgressListener sparkListener;

    private Future<Boolean> future;

    private StopWatch stopWatch;

    public Progress make() throws IOException {

        Callable<Boolean> callable = () -> {

            SparkSession sparkSession = getSparkSession();

            MakeModel.makeModel(sparkSession);

            return true;
        };

        if(future == null || future.isDone()) {
            stopWatch = StopWatch.createStarted();
            future = executorService.submit(callable);
        }

        return progress();
    }

    private SparkSession getSparkSession() {

//        if (sparkSession == null) {
            sparkSession = createSparkSession();

            sparkListener = setupListeners(sparkSession.sparkContext());
//        }
        return sparkSession;


    }

    private SparkSession createSparkSession(){
        return SparkSession.builder()
                .master(master)
                .appName("Daon Spark")
                .config("es.nodes", "localhost")
                .config("es.port", "9200")
                .config("es.index.auto.create", "false")
                .config("spark.ui.enabled", "false")
                .getOrCreate();
    }


    private boolean isRunning(){
        if(future != null){
            boolean isDone = future.isDone();
            if(isDone){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public void cancel(){
        if(sparkSession != null){
            sparkSession.sparkContext().cancelAllJobs();
        }
    }


    static JobProgressListener setupListeners(SparkContext context) {
        JobProgressListener pl = new JobProgressListener(context.getConf());
        context.addSparkListener(pl);
        return pl;
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

            log.info("progress : {}", progress);
        }

        return progress;
    }

    public byte[] model(String seq){
        GetResponse response = client.prepareGet(INDEX, TYPE, seq).setStoredFields("data").get();

        BytesArray bytesArray = (BytesArray) response.getField("data").getValue();

        return bytesArray.array();
    }

    public ModelInfo defaultModelInfo() throws IOException {

        ModelInfo modelInfo = ModelReader.create().load();

        return modelInfo;
    }

    public ModelInfo modelInfo(String seq) throws IOException {

        byte[] data = model(seq);

        InputStream inputStream = new ByteArrayInputStream(data);

        ModelInfo modelInfo = ModelReader.create().inputStream(inputStream).load();

        return modelInfo;
    }

    public String search(ModelParams params) {

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(params.getFrom())
                .setSize(params.getSize())
                .addSort("seq", SortOrder.DESC);

        log.info("query : {}", searchRequestBuilder);

//		if(filter.isDebug()) {
//			searchRequestBuilder.setExplain(true).setProfile(true); // Debug mode
//		}

        SearchResponse response = searchRequestBuilder
                .execute()
                .actionGet();

        return response.toString();
    }
}