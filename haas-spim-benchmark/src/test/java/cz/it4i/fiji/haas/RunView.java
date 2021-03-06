package cz.it4i.fiji.haas;

import java.util.concurrent.Executors;

import javax.swing.JFrame;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.ui.JobOutputView;

public class RunView {
	public static void main(String[] args) {
		new JobOutputView(new JFrame(), Executors.newSingleThreadExecutor(), null,
				SynchronizableFileType.StandardErrorFile, x -> "", 3000);
	}
}
