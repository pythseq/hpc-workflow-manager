package cz.it4i.fiji.haas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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

	@Parameter(label="Work directory",persist=true)
	private File workDirectory;
	
	@SuppressWarnings("unused")
	private JobManager jobManager;
	
	@Override
	public void run() {
		try {
			jobManager = new JobManager(getWorkingDirectoryPath());
		} catch (IOException e) {
			log.error(e);
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
