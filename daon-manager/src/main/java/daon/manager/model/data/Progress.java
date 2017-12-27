package daon.manager.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Progress {

    private String jobName;
    /**
     * 소요시간
     */
    private long elapsedTime;

    private int numCompletedJobs;
    private int numFailedJobs;

    private int numCompletedStages;
    private int numFailedStages;

    private boolean isRunning;

    public int getProgress(){
        return (int) ((numCompletedJobs / totalJobs()) * 100);
    }

    private float totalJobs(){
        if("sentences_to_words".equals(jobName)) return 4f;
        if("tag_trans".equals(jobName)) return 10f;
        return 8f;
    }
}
