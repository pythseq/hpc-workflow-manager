package cz.it4i.fiji.haas_snakemake_spim.commands;

import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.widget.UIComponent;

import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas_snakemake_spim.TestingConstants;
import cz.it4i.fiji.haas_snakemake_spim.ui.CheckStatusOfHaaSWindow;
import net.imagej.ImageJ;

/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Check status of HaaS")
public class CheckStatusOfHaaS implements Command {

	@Parameter
	private LogService log;

	@Parameter(label = "Work directory", persist = true, style = "directory")
	private File workDirectory;

	@Parameter
	private UIService uiService;

	@Parameter
	private Context context;

	private JobManager jobManager;

	@Override
	public void run() {
		jobManager = new JobManager(getWorkingDirectoryPath(), TestingConstants.getSettings());
		if (uiService.isHeadless()) {
			downloadAll();
		} else {
			CheckStatusOfHaaSWindow window;
			window = ModalDialogs.doModal(new CheckStatusOfHaaSWindow(getFrame(), context),
					WindowConstants.DISPOSE_ON_CLOSE);
			ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(window),
					WindowConstants.DO_NOTHING_ON_CLOSE);
			dialog.setTitle("Downloading info about jobs");
			Collection<JobInfo> jobs = jobManager.getJobs();
			int count = 0;
			for (JobInfo ji : jobs) {
				String item;
				dialog.addItem(item = "job id:" + ji.getId());
				window.addJob(ji);
				dialog.itemDone(item);
				dialog.setCount(count, jobs.size());
				count++;
			}
			dialog.done();
		}

	}

	private Frame getFrame() {
		ApplicationFrame af = uiService.getDefaultUI().getApplicationFrame();
		if (af instanceof Frame) {
			return (Frame) af;
		} else if (af instanceof UIComponent) {
			Object component = ((UIComponent<?>) af).getComponent();
			if (component instanceof Frame) {
				return (Frame) component;
			}
		}
		return null;
	}

	private void downloadAll() {
		for (JobInfo id : jobManager.getJobs()) {
			System.out.println("Job " + id.getId() + " needs download");
			jobManager.downloadJob(id.getId(), new DummyProgress());
		}
	}

	private Path getWorkingDirectoryPath() {
		return Paths.get(workDirectory.toString());
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(CheckStatusOfHaaS.class, true);
	}

}
