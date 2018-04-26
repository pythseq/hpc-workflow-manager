package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.UploadingFileImpl;

public class Synchronization {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.Synchronization.class);
	
	private static final String FILE_INDEX_TO_UPLOAD_FILENAME = ".toUploadFiles";
	
	private static final String FILE_INDEX_TO_DOWNLOAD_FILENAME = ".toDownloadFiles";
	
	private static final String FILE_INDEX_DOWNLOADED_FILENAME = ".downloaded";
	
	private Path workingDirectory;
	
	private Function<String,Path> pathResolver = name -> workingDirectory.resolve(name);
	
	private PersistentIndex<Path> filesDownloaded;

	private PersitentSynchronizationProcess<Path> uploadProcess;

	private P_PersistentDownloadProcess downloadProcess;
	
	
	
	public Synchronization(Supplier<HaaSFileTransfer> fileTransferSupplier, Path workingDirectory,
			ExecutorService service, Runnable uploadFinishedNotifier, Runnable downloadFinishedNotifier) throws IOException {

		this.workingDirectory = workingDirectory;
		this.filesDownloaded = new PersistentIndex<>(workingDirectory.resolve(FILE_INDEX_DOWNLOADED_FILENAME),
				pathResolver);
		this.uploadProcess = createUploadProcess(fileTransferSupplier, service, uploadFinishedNotifier);
		this.downloadProcess = createDownloadProcess(fileTransferSupplier, service, downloadFinishedNotifier);
	}

	public synchronized void startUpload() throws IOException {
		uploadProcess.start();
	}
	
	public void stopUpload() throws IOException {
		uploadProcess.stop();
	}

	public void resumeUpload() {
		uploadProcess.resume();
	}
	
	public synchronized void startDownload(Collection<String> files) throws IOException {
		this.downloadProcess.setItems(files);
		this.downloadProcess.start();
	}
	
	public synchronized void stopDownload() throws IOException {
		this.downloadProcess.stop();
	}
	
	public synchronized void resumeDownload() {
		this.downloadProcess.resume();
	}

	private boolean canUpload(Path file) {
		
		return !file.getFileName().toString().matches("[.][^.]+") && !filesDownloaded.contains(file);
	}

	private PersitentSynchronizationProcess<Path> createUploadProcess(Supplier<HaaSFileTransfer> fileTransferSupplier,
			ExecutorService service, Runnable uploadFinishedNotifier) throws IOException {
		return new PersitentSynchronizationProcess<Path>("upload", service, fileTransferSupplier, uploadFinishedNotifier,
				workingDirectory.resolve(FILE_INDEX_TO_UPLOAD_FILENAME), pathResolver) {

			@Override
			protected Iterable<Path> getItems() throws IOException {
				try(DirectoryStream<Path> ds = Files.newDirectoryStream(workingDirectory,Synchronization.this::canUpload)) {
					return StreamSupport.stream(ds.spliterator(), false).collect(Collectors.toList()); 
				}
			}
	
			@Override
			protected void processItem(HaaSFileTransfer tr, Path p) {
				UploadingFile uf = new UploadingFileImpl(p);
				tr.upload(uf);
			}
		};
	}

	private P_PersistentDownloadProcess createDownloadProcess(
			Supplier<HaaSFileTransfer> fileTransferSupplier,  ExecutorService service,
			Runnable uploadFinishedNotifier) throws IOException {
		
		return new P_PersistentDownloadProcess(service, fileTransferSupplier, uploadFinishedNotifier);
	}
	
	private class P_PersistentDownloadProcess extends PersitentSynchronizationProcess<String>{

		private Collection<String> items = Collections.emptyList();
		
		public P_PersistentDownloadProcess(ExecutorService service, Supplier<HaaSFileTransfer> fileTransferSupplier,
				Runnable processFinishedNotifier) throws IOException {
			super("download",service, fileTransferSupplier, processFinishedNotifier,
					workingDirectory.resolve(FILE_INDEX_TO_DOWNLOAD_FILENAME), name -> name);
		}

		private synchronized void setItems(Collection<String> items) {
			this.items = new LinkedList<>(items);
		}
		
		@Override
		protected synchronized Iterable<String> getItems() throws IOException {
			return items;
		}

		@Override
		protected void processItem(HaaSFileTransfer tr, String file) {
			filesDownloaded.insert(workingDirectory.resolve(file));
			try {
				filesDownloaded.storeToFile();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			tr.download(file, workingDirectory);
		}
		
	}
}
