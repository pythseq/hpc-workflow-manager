
package cz.it4i.fiji.haas.ui;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public interface JavaFXRoutines {

	Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.CloseableControl.class);

	static void initRootAndController(String string, Object parent) {
		initRootAndController(string, parent, false);
	}

	static void initRootAndController(String string, Object parent, boolean setController) {
		FXMLLoader fxmlLoader = new FXMLLoader(parent.getClass().getResource(string));
		fxmlLoader.setControllerFactory(c -> {
			try {
				if (c.equals(parent.getClass())) {
					return parent;
				}
				return c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
		fxmlLoader.setRoot(parent);
		if (setController) {
			fxmlLoader.setController(parent);
		}
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		if (fxmlLoader.getController() == null) {
			throw new IllegalStateException("Not set controller.");
		}

	}

	@SuppressWarnings("unchecked")
	static public <U, T extends ObservableValue<U>, V> void setCellValueFactory(TableView<T> tableView, int index,
			Function<U, V> mapper) {
		((TableColumn<T, V>) tableView.getColumns().get(index))
				.setCellValueFactory(f -> new ObservableValueAdapter<>(f.getValue(), mapper));
	}

	@SuppressWarnings("unchecked")
	static public <T, V> void setCellValueFactoryForList(TableView<T> tableView,
		int index,
		Callback<TableColumn.CellDataFeatures<T, V>, ObservableValue<V>> callback)
	{
		((TableColumn<T, V>) tableView.getColumns().get(index)).setCellValueFactory(
			callback);
	}

	static public RunnableFuture<Void> runOnFxThread(Runnable runnable) {

		RunnableFuture<Void> result = new FutureTask<>(runnable, null);

		if (Platform.isFxApplicationThread()) {
			result.run();
		}
		else {
			Platform.runLater(result);
		}
		return result;
	}

	// TODO move to own class in the future
	static public <V> void executeAsync(Executor executor, Callable<V> action, Consumer<V> postAction) {
		executor.execute(() -> {
			V result;
			try {
				result = action.call();
				postAction.accept(result);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});
	}
	
	public static <T> boolean notNullValue(ObservableValue<T> j, Predicate<T> pred) {
		if (j == null || j.getValue() == null) {
			return false;
		}
		return pred.test(j.getValue());
	}

	static public <T, U extends ObservableValue<T>> void setOnDoubleClickAction(
		TableView<U> tableView, ExecutorService executorService,
		Predicate<U> openAllowed, Consumer<U> r)
	{
		tableView.setRowFactory(tv -> {
			TableRow<U> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					U rowData = row.getItem();
					if (openAllowed.test(rowData)) {
						executorService.execute(() -> r.accept(rowData));
					}
				}
			});
			return row;
		});
	}

	public static String toCss(Color color) {
		return "rgb(" + Math.round(color.getRed() * 255.0) + "," + Math.round(color
			.getGreen() * 255.0) + "," + Math.round(color.getBlue() * 255.0) + ")";
	}
}
