package com.itq.document_station.controller;

import com.itq.document_station.dto.*;
import com.itq.document_station.enumeration.ProcessingResult;
import com.itq.document_station.exception.AuthenticationException;
import com.itq.document_station.service.DocService;
import com.itq.document_station.service.generate.DocGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/doc")
public class DocController {

    @Autowired
    private DocService docService;

    @Autowired
    private DocGenerateService docGenerateService;

    /**
     * Создание документа
     * @param userDetails
     * @param request
     * @return MessageResponse
     */
    @PostMapping("/create")
    public ResponseEntity<DocDto> create(@AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @RequestBody DocCreateRequest request) {
        if (userDetails == null) throw new AuthenticationException("Not authenticated");
        log.info("[REST]: запрос на создание документа юзером: {}", userDetails.getUsername());
        DocDto dto = docService.createDoc(userDetails, request);
        return ResponseEntity.ok(dto);
    }

    /**
     * Пакетная генерация документов (кол-во документов в конфиге)
     * @param userDetails
     * @return MessageResponse
     */
    @PostMapping("/generate-batch")
    public ResponseEntity<?> generate(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) throw new AuthenticationException("Not authenticated");
        log.info("[REST]: запрос на массовую генерацию документов юзером: {}", userDetails.getUsername());
        Long ms = docGenerateService.createDocs(userDetails);
        String message = "Документы успешно созданы за " + ms + " mc";
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK.value(), message));
    }

    /**
     * Поиск документа по id
     * @param id
     * @return
     */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<DocWithHistoryDto> findOneById(@PathVariable(name = "id") Long id) {
        log.info("[REST]: запрос на поиск документа по id: {}", id);
        DocWithHistoryDto dto = docService.findById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Поиск документов по списку ids
     * @param request
     * @return
     */
    @PostMapping("/search")
    public ResponseEntity<PageDto<DocDto>> findBatch(@RequestBody @Valid DocSearchRequest request) {
        log.info("[REST]: запрос на поиск документов");
        PageDto<DocDto> result = docService.findBatch(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit")
    public ResponseEntity<DocProcessResponse> submit(@AuthenticationPrincipal UserDetails userDetails,
                                                              @Valid @RequestBody DocProcessRequest request) {

        log.info("[REST]: запрос на согласование документов юзером: {}", userDetails.getUsername());
        Map<Long, ProcessingResult> results = docService.submitDocuments(request.getIds(),
                userDetails,
                "Операция выполнена через вызов API");
        log.info("[REST]: согласование {} документов завершено", request.getIds().size());
        DocProcessResponse response = new DocProcessResponse();
        response.setResults(convertResults(results));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    public ResponseEntity<DocProcessResponse> approve(@AuthenticationPrincipal UserDetails userDetails,
                                                     @Valid @RequestBody DocProcessRequest request) {

        log.info("[REST]: запрос на утверждение документов юзером: {}", userDetails.getUsername());
        Map<Long, ProcessingResult> results = docService.approveDocuments(request.getIds(),
                userDetails,
                "Операция выполнена через вызов API");
        log.info("[REST]: утверждение {} документов завершено", request.getIds().size());
        DocProcessResponse response = new DocProcessResponse();
        response.setResults(convertResults(results));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/concurrent")
    public ResponseEntity<ConcurrentApproveResponse> concurrentApprove(@AuthenticationPrincipal UserDetails userDetails,
                                                                           @Valid @RequestBody ConcurrentApproveRequest request) {
        log.info("[REST]: запрос на конкурентную регистрацию документа юзером: {}", userDetails.getUsername());
        ConcurrentApproveResponse response = docService.concurrentApprove(userDetails, request, "утверждение и регистрация завершены конкуретно");
        return ResponseEntity.ok(response);
    }

    private Map<Long, String> convertResults(Map<Long, ProcessingResult> results) {
        return results.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().name(),
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

}
