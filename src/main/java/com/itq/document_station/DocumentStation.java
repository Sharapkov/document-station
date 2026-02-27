package com.itq.document_station;

import com.itq.document_station.config.AppConfig;
import com.itq.document_station.service.generate.DocGenerateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.NumberFormat;
import java.util.Collections;

@SpringBootApplication
public class DocumentStation implements CommandLineRunner {

	private static Log logger = LogFactory.getLog(DocumentStation.class);

	@Autowired
	private AppConfig appConfig;

	public static void main(String[] args) {
		SpringApplication.run(DocumentStation.class, args);
	}

		@Override
		public void run (String...args){
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

	// исследование пакетного создания vs одиночного создания
//	--------------------------------------------------
//	Regular inserts     |    1         |           6ms
//	Batch inserts       |    1         |           2ms
//	Total gain: 66 %
//			--------------------------------------------------
//	Regular inserts     |    10        |           5ms
//	Batch inserts       |    10        |           2ms
//	Total gain: 60 %
//			--------------------------------------------------
//	Regular inserts     |    100       |          37ms
//	Batch inserts       |    100       |           9ms
//	Total gain: 75 %
//			--------------------------------------------------
//	Regular inserts     |    1000      |         286ms
//	Batch inserts       |    1000      |         103ms
//	Total gain: 63 %
//			--------------------------------------------------
//	Regular inserts     |    10000     |        3910ms
//	Batch inserts       |    10000     |         970ms
//	Total gain: 75 %
//			--------------------------------------------------
//	Regular inserts     |    100000    |       34582ms
//	Batch inserts       |    100000    |        9116ms
//	Total gain: 73 %
//			--------------------------------------------------
//	Regular inserts     |    1000000   |      326556ms
//	Batch inserts       |    1000000   |       94127ms
//	Total gain: 71 %


	// авторизация + текущий пользак работают
	// таблицы с документами созданы
	// find-by-id, findAll работают
	// параллельная обработка (да)
    // фоновые процессы (да)
    // пкетная вставка (да)
	// Проверка конкурентного утверждения (да)


// проверено, документация написана


// todo: добавить:
//  3 explain sql (1 час)
// todo: GIT (чекнуть с ноута)


// todo: создать docker compose, бд + миграция + тестовые юзеры миграцию данных









