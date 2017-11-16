package cz.it4i.fiji.haas.ui;

import java.util.function.Function;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;

public class CheckStatusOfHaaSController {

	@Parameter
	private LogService logService;

	@FXML
	private TableView<JobInfo> jobs;

	public CheckStatusOfHaaSController() {

	}

	public void addJob(JobInfo job) {
		jobs.getItems().add(job);
	}

	public void init() {
		initTable();
		initMenu();
	}

	private void initMenu() {
		ContextMenu cm = new ContextMenu();
		MenuItem download = new MenuItem("Download");
		download.setOnAction(ev->jobs.getSelectionModel().getSelectedItem().downloadData());
		cm.getItems().add(download);
		jobs.setContextMenu(cm);
		jobs.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				if (jobs.getSelectionModel().getSelectedCells().size() < 1) {
					return;
				}
				JobInfo job = jobs.getSelectionModel().getSelectedItem();
				
				if (job != null && job.needsDownload()) {
					download.setDisable(false);
				} else {
					download.setDisable(true);
				}
			}
		});
	}

	private void initTable() {
		setCellValueFactory(0, j -> j.getId().toString());
		setCellValueFactory(1, j -> j.getState().toString() + (j.needsDownload() ? " - needs download" : ""));
		setCellValueFactory(2, j -> j.getStartTime().toString());
		setCellValueFactory(3, j -> j.getEndTime().toString());
	}

	@SuppressWarnings("unchecked")
	private void setCellValueFactory(int index, Function<JobInfo, String> mapper) {
		((TableColumn<JobInfo, String>) jobs.getColumns().get(index))
				.setCellValueFactory(f -> new ObservableValueAdapter<JobInfo, String>(f.getValue(), mapper));

	}
}
