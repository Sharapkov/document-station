package com.itq.document_station.worker;

import com.itq.document_station.config.AppConfig;
import com.itq.document_station.enumeration.ProcessingResult;
import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.model.Doc;
import com.itq.document_station.repository.DocRepository;
import com.itq.document_station.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitWorker {

    private final DocRepository docRepository;
    private final DocService docService;
    private final AppConfig appConfig;

    @Scheduled(fixedDelayString = "${app.worker.submit.fixed-delay}")
    public void processPendingSubmits() {
        int batchSize = appConfig.getSUBMIT_SIZE();

        StopWatch stopWatch = new StopWatch("SubmitWorker");
        stopWatch.start("согласование документов");

        log.info("[SubmitWorker]: запуск, максимальное кол-во документ на согласование: {}", batchSize);
        Pageable pageable = PageRequest.of(0, batchSize);
        List<Doc> docs = docRepository.findByStatusOrderByIdAsc(StatusEnum.DRAFT, pageable);

        if (docs.isEmpty()) {
            log.info("[SubmitWorker]: нет документов для обработки");
            return;
        }

        List<Long> ids = docs.stream().map(Doc::getId).collect(Collectors.toList());
        log.info("[SubmitWorker]: распределяю {} документов на {}-{} потоков",
                ids.size(), appConfig.getSubmitCorePoolSize(), appConfig.getSubmitMaxPoolSize());

        Map<Long, ProcessingResult> results = docService.submitDocuments(ids, null, "создано фоновой задачей");

        // результаты
        long successCount = results.values().stream().filter(r -> r == ProcessingResult.SUCCESS).count();
        long conflictCount = results.values().stream().filter(r -> r == ProcessingResult.CONFLICT).count();
        long notFoundCount = results.values().stream().filter(r -> r == ProcessingResult.NOT_FOUND).count();
        stopWatch.stop();

        log.info("[SubmitWorker]: завершён за {} сек. Успех: {}, конфликт: {}, не найдено: {}",
                stopWatch.getTotalTimeSeconds(),
                successCount, conflictCount, notFoundCount);

    }
}
