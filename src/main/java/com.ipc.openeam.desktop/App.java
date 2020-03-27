package com.ipc.openeam.desktop;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.ipc.openeam.desktop.dao.*;
import com.ipc.openeam.desktop.module.ProductionModule;
import com.ipc.openeam.desktop.exchange.point.in.XMLIn;
import com.ipc.openeam.desktop.exchange.point.out.DAOOut;
import com.ipc.openeam.desktop.exchange.system.ExternalSys.FromExternalSystem;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {
	private final Injector injector = Guice.createInjector(new ProductionModule(), new JpaPersistModule("production"));
	public static final Logger log = Logger.getLogger(Application.class);


	public static final Map<String, String> cmdArguments = new HashMap<>();
	
	public static void main() {
	}


	@Override
	public void start(Stage stage) throws Exception {

		Service startService = new Service() {
			@Override
			protected Task createTask() {
				Task task = new Task() {
					@Override
					protected Object call() throws Exception {
						initApp();
						return null;
					}
				};

				return task;
			}
		};
		startService.start();
	}

	private void initApp() throws Exception {

		File metaFile = new File("meta.xml");
		if (metaFile.exists()) {
			log.info("Meta file exist, Proceed");

			try {
				loadMeta(metaFile);
			} catch (Throwable metaErr) {
				log.info("Error loading metafile");
			}

		}
	}

	private void loadMeta(File metaFile) throws Exception {

		FileInputStream inputStream = new FileInputStream(metaFile); // io
		XMLIn in = new XMLIn(inputStream);
		FromExternalSystem system = new FromExternalSystem();
		system.setIn(in);

		DAOOut out = injector.getInstance(DAOOut.class);
		out.setRemoveMissed(true);

		// Фактическая загрузка данных
		out.flush(system);

		inputStream.close();

	}

}
