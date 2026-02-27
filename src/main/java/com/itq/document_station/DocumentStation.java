package com.itq.document_station;

import com.itq.document_station.config.AppConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.NumberFormat;

@SpringBootApplication
public class DocumentStation implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(DocumentStation.class);

	@Autowired
	private AppConfig appConfig;

	public static void main(String[] args) {
		SpringApplication.run(DocumentStation.class, args);
	}

	@Override
	public void run(String... args) {
		Runtime runtime = Runtime.getRuntime();

		final NumberFormat format = NumberFormat.getInstance();

		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final long mb = 1024 * 1024;
		final String mega = " MB";

		logger.info("========================== Memory Info ==========================");
		logger.info("Free memory: " + format.format(freeMemory / mb) + mega);
		logger.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
		logger.info("Max memory: " + format.format(maxMemory / mb) + mega);
		logger.info("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega);
		logger.info("=================================================================\n");
		logger.info("Тестовые юзеры:");
		logger.info("{\"username\": \"Поросенок Петр\", \"password\": \"password1\"}");
		logger.info("{\"username\": \"Колобок Виктор\", \"password\": \"password2\"}");
		logger.info("{\"username\": \"Крокодил Гена\", \"password\": \"password3\"}");
		logger.info("{\"username\": \"Еж Василий\", \"password\": \"password4\"}");
		logger.info("{\"username\": \"Синица Николай\", \"password\": \"password5\"} // system user (author by default)");
		logger.info("=================================================================\n");

		logger.info("[Конфигурация приложения]: кол-во документов, которое закидываем на " +
				appConfig.getSubmitCorePoolSize() + "-" + appConfig.getSubmitMaxPoolSize() +
				" потоков submit: " + appConfig.getSUBMIT_SIZE());
		logger.info("[Конфигурация приложения]: кол-во документов, которое закидываем на " +
				appConfig.getApproveCorePoolSize() + "-" + appConfig.getApproveMaxPoolSize() +
				" потоков approve: " + appConfig.getAPPROVE_SIZE());
		logger.info("[Конфигурация приложения]: кол-во документов, которое обрабатываем в одном потоке: " +
				appConfig.getPARTITION_SIZE());
	}
}