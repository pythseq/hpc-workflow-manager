
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.concurrent.Executor;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;

public class FXFrameExecutorService implements Executor {

	@Override
	public void execute(Runnable command) {

		JavaFXRoutines.runOnFxThread(() -> {
			command.run();
		});

	}

}
