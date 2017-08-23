package daon.manager.model.data;

import daon.analysis.ko.model.Keyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Progress {

    private static final float TOTAL_JOBS = 18f;

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
        return (int) ((numCompletedJobs / TOTAL_JOBS) * 100);
    }
}
