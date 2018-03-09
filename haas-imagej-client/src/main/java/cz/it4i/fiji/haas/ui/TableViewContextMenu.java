package cz.it4i.fiji.haas.ui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;

public class TableViewContextMenu<T> {

	public final static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.TableViewContextMenu.class);

	private final Collection<P_MenuItem> items = new LinkedList<>();
	private final Collection<P_MenuItemWithColumnIndex> itemsWithColumnIndex = new LinkedList<>();

	private TableView<T> tableView;

	private int columnIndex = -1;

	public TableViewContextMenu(TableView<T> tableView) {
		this.tableView = tableView;
	}

	public void addItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
		items.add(new P_MenuItem(text, eventHandler, enableHandler));
	}

	public void addItem(String text, BiConsumer<T, Integer> eventHandler, BiPredicate<T, Integer> enableHandler) {
		itemsWithColumnIndex.add(new P_MenuItemWithColumnIndex(text, eventHandler, enableHandler));
	}

	private T getRequestedItem() {
		return tableView.getFocusModel().getFocusedItem();
	}

	private int getRequestedColumn() {
		return columnIndex;
	}

	private ContextMenu getOrCreateContextMenu() {
		ContextMenu cm = tableView.getContextMenu();
		if (cm == null) {
			cm = new ContextMenu();
			tableView.setContextMenu(cm);
			tableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
				@Override
				public void handle(ContextMenuEvent event) {
					T requestedItem = getRequestedItem();
					updateColumnIndex(event.getSceneX());
					int column = getRequestedColumn();
					for (P_MenuItem item : items) {
						item.updateEnable(requestedItem);
					}
					itemsWithColumnIndex.forEach(item -> item.updateEnable(requestedItem, column));
				}

				private void updateColumnIndex(double sceneX) {
					double last = 0;
					columnIndex = tableView.getColumns().size();
					int index = 0;
					for (TableColumn<?, ?> column : tableView.getColumns()) {
						last += column.getWidth();
						if (last > sceneX) {
							columnIndex = index;
							break;
						}
						index++;
					}
				}
			});

		}
		return cm;
	}

	private class P_MenuItem {

		private MenuItem item;
		private Predicate<T> enableHandler;

		public P_MenuItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
			this.enableHandler = enableHandler;
			item = new MenuItem(text);
			item.setOnAction(e -> eventHandler.accept(getRequestedItem()));
			getOrCreateContextMenu().getItems().add(item);
		}

		public void updateEnable(T selected) {
			item.setDisable(!enableHandler.test(selected));
		}

	}

	private class P_MenuItemWithColumnIndex {

		private MenuItem item;
		private BiPredicate<T, Integer> enableHandler;

		public P_MenuItemWithColumnIndex(String text, BiConsumer<T, Integer> eventHandler,
				BiPredicate<T, Integer> enableHandler) {
			this.enableHandler = enableHandler;
			item = new MenuItem(text);
			item.setOnAction(e -> eventHandler.accept(getRequestedItem(), getRequestedColumn()));
			getOrCreateContextMenu().getItems().add(item);
		}

		public void updateEnable(T selected, int column) {
			item.setDisable(!enableHandler.test(selected, column));
		}

	}

}
