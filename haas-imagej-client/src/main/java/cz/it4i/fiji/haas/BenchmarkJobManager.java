package cz.it4i.fiji.haas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private JobManager jobManager;
	private Progress progress;
	private Path workDirectory;

	private static final String CONFIG_FOR_MODIFICATION = "not-set-config.yaml";
	private static final String CONFIG_MODIFIED = "config.yaml";

	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		this.workDirectory = workDirectory;
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(3, 6));
		this.progress = progress;
	}

	public JobInfo startJob() throws IOException {
		
		JobInfo jobInfo = jobManager.startJob(Arrays.asList(getUploadingFile()).stream(), progress);
		jobInfo.waitForStart();
		if (jobInfo.getState() != JobState.Running) {
			throw new IllegalStateException("start of job: " + jobInfo + " failed");
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jobInfo.downloadFileData(CONFIG_FOR_MODIFICATION, os);
		byte[] data = updateConfigFile(os.toByteArray());

		jobInfo.uploadFile(new ByteArrayInputStream(data), CONFIG_MODIFIED, data.length,
				Instant.now().getEpochSecond());
		return jobInfo;
	}

	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", "config.yaml");
	}

	public JobState getState(long jobId) {
		return jobManager.getState(jobId);
	}
	

	private byte[] updateConfigFile(byte[] data) throws IOException {
		return data;

	}

}
