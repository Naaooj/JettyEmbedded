package fr.naoj.embeddedjetty.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchLauncher {

	private static final Logger LOG = Logger.getLogger(BatchLauncher.class.getName());
	
	private final Job job;

    private final JobLauncher jobLauncher;

    @Autowired
    BatchLauncher(@Qualifier("excelFileToXmlJob") Job job, JobLauncher jobLauncher) {
        this.job = job;
        this.jobLauncher = jobLauncher;
    }
    
    @Scheduled(cron = "${excel.to.xml.job.cron}")
    void launchExcelFileToXmlJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        LOG.info("Starting job");

        jobLauncher.run(job, createJobParameters());

        LOG.info("Stopping job");
    }

    private JobParameters createJobParameters() {
        Map<String, JobParameter> parameters = new HashMap<>();

        JobParameter parameter = new JobParameter(new Date());
        parameters.put("currentTime", parameter);

        return new JobParameters(parameters);
    }
}
