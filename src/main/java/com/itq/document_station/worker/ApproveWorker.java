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
public class ApproveWorker {

    private final DocRepository docRepository;
    private final DocService docService;
    private final AppConfig appConfig;

    @Scheduled(fixedDelayString = "${app.worker.approve.fixed-delay}")
    public void processPendingApprove() {
        int batchSize = appConfig.getAPPROVE_SIZE();

        StopWatch stopWatch = new StopWatch("ApproveWorker");
        stopWatch.start("утверждение документов");

        log.info("[ApproveWorker]: запуск, максимальное кол-во документов на утверждение: {}", batchSize);
        Pageable pageable = PageRequest.of(0, batchSize);
        List<Doc> docs = docRepository.findByStatusOrderByIdAsc(StatusEnum.SUBMITTED, pageable);

        if (docs.isEmpty()) {
            log.info("[ApproveWorker]: нет документов для обработки");
            return;
        }

        List<Long> ids = docs.stream().map(Doc::getId).collect(Collectors.toList());
        log.info("[ApproveWorker]: распределяю {} документов на {}-{} потоков",
                ids.size(), appConfig.getApproveCorePoolSize(), appConfig.getApproveMaxPoolSize());

        Map<Long, ProcessingResult> results = docService.approveDocuments(ids, null, "создано фоновой задачей");

        // результаты
        long successCount = results.values().stream().filter(r -> r == ProcessingResult.SUCCESS).count();
        long conflictCount = results.values().stream().filter(r -> r == ProcessingResult.CONFLICT).count();
        long notFoundCount = results.values().stream().filter(r -> r == ProcessingResult.NOT_FOUND).count();
        stopWatch.stop();

        log.info("[ApproveWorker]: завершён за {} сек. Успех: {}, конфликт: {}, не найдено: {}",
                stopWatch.getTotalTimeSeconds(),
                successCount, conflictCount, notFoundCount);

    }
}
