package com.itq.document_station.service.impl;

import com.itq.document_station.config.AppConfig;
import com.itq.document_station.dto.*;
import com.itq.document_station.enumeration.ActionEnum;
import com.itq.document_station.enumeration.ProcessingResult;
import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.exception.InvalidOperationException;
import com.itq.document_station.model.Doc;
import com.itq.document_station.model.History;
import com.itq.document_station.model.Register;
import com.itq.document_station.model.User;
import com.itq.document_station.repository.DocRepository;
import com.itq.document_station.repository.RegisterRepository;
import com.itq.document_station.repository.UserRepository;
import com.itq.document_station.repository.specification.DocSpecifications;
import com.itq.document_station.service.DocService;
import com.itq.document_station.utill.DocMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocServiceImpl implements DocService {


    private final UserRepository userRepository;
    private final DocRepository docRepository;
    private final RegisterRepository registerRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final Executor taskSubmitExecutor;
    private final Executor taskApproveExecutor;
    private final AppConfig appConfig;

    @Autowired
    public DocServiceImpl(UserRepository userRepository,
                          DocRepository docRepository,
                          RegisterRepository registerRepository,
                          EntityManager entityManager,
                          TransactionTemplate transactionTemplate,
                          @Qualifier("taskSubmitExecutor") Executor taskSubmitExecutor,
                          @Qualifier("taskApproveExecutor") Executor taskApproveExecutor,
                          AppConfig appConfig) {
        this.userRepository = userRepository;
        this.docRepository = docRepository;
        this.registerRepository = registerRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
        this.taskSubmitExecutor = taskSubmitExecutor;
        this.taskApproveExecutor = taskApproveExecutor;
        this.appConfig = appConfig;
    }


    @Override
    public DocDto createDoc(UserDetails userDetails, DocCreateRequest request) {
        User author = getAuthorOrSystemUser(userDetails);

        Doc doc = new Doc();
        doc.setUser(author);
        doc.setName(request.getName());
        doc.setStatus(StatusEnum.DRAFT);
        doc.setCreatedDate(LocalDateTime.now());
        doc.setUpdatedDate(LocalDateTime.now());

        docRepository.save(doc);
        log.info("[DocService]:[Create]: успешно");
        return DocMapper.mapToDto(doc);
    }

    @Override
    public DocWithHistoryDto findById(Long id) {
        Doc doc = docRepository.findByIdWithHistory(id)
                .orElseThrow(() -> new EntityNotFoundException("Документ не найден"));
        log.info("[DocService]:[Find by id]: успешно");
        return DocMapper.mapToDocWithHistoryDto(doc);
    }

    @Override
    public PageDto<DocDto> findBatch(DocSearchRequest request) {
        Specification<Doc> spec = Specification.where(DocSpecifications.idIn(request.getIds()));
        if (request.getStatus() != null) {
            spec = spec.and(DocSpecifications.hasStatus(request.getStatus()));
        }
        if (request.getAuthorId() != null) {
            spec = spec.and(DocSpecifications.hasAuthorId(request.getAuthorId()));
        }
        if (request.getDateFrom() != null || request.getDateTo() != null) {
            spec = spec.and(DocSpecifications.byDate(request.getDateFrom(), request.getDateTo()));
        }

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                request.getSortDirection(),
                request.getSortBy()
        );

        Page<Doc> page = docRepository.findAll(spec, pageable);
        List<DocDto> content = DocMapper.mapToList(page.getContent());

        PageDto<DocDto> dto = new PageDto<>();
        dto.setContent(content);
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setLast(page.isLast());
        log.info("[DocService]:[Find batch]: успешно");
        return dto;
    }

    @Override
    public Map<Long, ProcessingResult> submitDocuments(List<Long> ids,
                                                       UserDetails userDetails,
                                                       String comment) {
        log.info("[DocService]:[SUBMIT]: начинаю работу, общий размер: {}", ids.size());
        User author = getAuthorOrSystemUser(userDetails);

        // общий счетчик
        AtomicInteger count = new AtomicInteger(0);
        int total = ids.size();

        // ids разбиты на части, каждая часть обрабатывается в своем потоке
        List<List<Long>> partitionsIds = separationToPartitions(ids, appConfig.getPARTITION_SIZE());

        List<CompletableFuture<Map<Long, ProcessingResult>>> futures = partitionsIds.stream()
                .map(partIds -> CompletableFuture.supplyAsync(
                        () -> processSubmit(partIds, author, comment, count, total), taskSubmitExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Long, ProcessingResult> processSubmit(List<Long> partIds,
                                                      User author, String comment,
                                                      AtomicInteger count, int total) {
        log.info("[DocService]:[SUBMIT]: начинаю обработку {} документа(ов)...", partIds.size());
        Map<Long, ProcessingResult> results = new HashMap<>();
        for (Long id : partIds) {
            results.put(id, submitDoc(id, author, comment));
            int done = count.incrementAndGet();
            log.info("[DocService]:[SUBMIT]:Прогресс соглсования: обработано {}/{}, осталось {}", done, total, total - done);
        }
        return results;
    }

    private ProcessingResult submitDoc(long id, User author, String comment) {
        return transactionTemplate.execute(status -> {
            try {
                Doc document = docRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Документ не найден"));

                if (document.getStatus() != StatusEnum.DRAFT) {
                   throw new IllegalArgumentException("Неверный статус документа");
                }

                document.setStatus(StatusEnum.SUBMITTED);
                document.setUpdatedDate(LocalDateTime.now());

                History history = new History();
                history.setAction(ActionEnum.SHIFT_SUBMITTED);
                history.setCreatedDate(LocalDateTime.now());
                history.setUser(author);
                history.setComment(comment);
                history.setDoc(document);

                document.getHistories().add(history);
                docRepository.save(document);

                //log.info("[DocService]:[SUBMIT]: успешно обновлен документ c id: {} ", document.getId());
                return ProcessingResult.SUCCESS;

            } catch (EntityNotFoundException e) {
                status.setRollbackOnly();
                return ProcessingResult.NOT_FOUND;
            } catch (Exception e) {
                status.setRollbackOnly();
                return ProcessingResult.CONFLICT;
            }
        });
    }

    @Override
    public Map<Long, ProcessingResult> approveDocuments(List<Long> ids,
                                                        UserDetails userDetails,
                                                        String comment) {
        log.info("[DocService]:[APPROVE]: начинаю работу, общий размер: {}", ids.size());
        User author = getAuthorOrSystemUser(userDetails);

        // общий счетчик
        AtomicInteger count = new AtomicInteger(0);
        int total = ids.size();

        // ids разбиты на части, каждая часть обрабатывается в своем потоке
        List<List<Long>> partitionsIds = separationToPartitions(ids, appConfig.getPARTITION_SIZE());

        List<CompletableFuture<Map<Long, ProcessingResult>>> futures = partitionsIds.stream()
                .map(partIds -> CompletableFuture.supplyAsync(
                        () -> processApprove(partIds, author, comment, count, total), taskApproveExecutor))
                .toList();

        // Ждём завершения всех и объединяем результаты
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Long, ProcessingResult> processApprove(List<Long> partIds,
                                                       User author,
                                                       String comment,
                                                       AtomicInteger count,
                                                       int total) {
        log.info("[DocService]:[APPROVE]: начинаю обработку {} документа(ов)...", partIds.size());
        Map<Long, ProcessingResult> results = new HashMap<>();
        for (Long id : partIds) {
            results.put(id, approveDoc(id, author, comment));
            int done = count.incrementAndGet();
            log.info("[DocService]:[APPROVE]:Прогресс утверждения: обработано {}/{}, осталось {}", done, total, total - done);
        }
        return results;
    }

    private ProcessingResult approveDoc(long id, User author, String comment) {
        return transactionTemplate.execute(status -> {
            try {
                Doc document = docRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Документ не найден"));

                if (document.getStatus() != StatusEnum.SUBMITTED) {
                    throw new IllegalArgumentException("Неверный статус документа");
                }

                document.setStatus(StatusEnum.APPROVED);
                document.setUpdatedDate(LocalDateTime.now());

                History history = new History();
                history.setAction(ActionEnum.SHIFT_APPROVED);
                history.setCreatedDate(LocalDateTime.now());
                history.setUser(author);
                history.setComment(comment);
                history.setDoc(document);

                document.getHistories().add(history);
                docRepository.save(document);

                Register register = new Register();
                String message = "Документ id: " + id + " утвержден пользователем: " + author.getUsername();
                register.setMessage(message);
                register.setCreatedDate(LocalDateTime.now());
                register.setDoc(document);
                registerRepository.save(register);

                return ProcessingResult.SUCCESS;

            } catch (EntityNotFoundException e) {
                status.setRollbackOnly();
                return ProcessingResult.NOT_FOUND;
            } catch (Exception e) {
                status.setRollbackOnly();
                return ProcessingResult.CONFLICT;
            }
        });
    }

    @Override
    public ConcurrentApproveResponse concurrentApprove(UserDetails userDetails,
                                                       ConcurrentApproveRequest request,
                                                       String comment) {
        log.info("[DocService]:[CONCURRENT]: для гонки выделено {} потока(ов), по {} попытке(ок)",
                request.getThreads(), request.getAttempts());

        Doc doc = docRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Документ не найден"));
        if (doc.getStatus() != StatusEnum.SUBMITTED) {
            throw new InvalidOperationException("Документ должен быть в статусе SUBMITTED, текущий статус: " + doc.getStatus());
        }

        User author = getAuthorOrSystemUser(userDetails);

        int threads = request.getThreads();
        int attempts = request.getAttempts();
        int totalAttempts = threads * attempts;

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);
        AtomicInteger error = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // ждем общего старта
                    for (int j = 0; j < attempts; j++) {
                        try {
                            ProcessingResult result = approveDoc(
                                    request.getDocumentId(),
                                    author,
                                    comment
                            );
                            switch (result) {
                                case SUCCESS -> success.incrementAndGet();
                                case CONFLICT, NOT_FOUND -> conflict.incrementAndGet();
                            }
                        } catch (Exception e) {
                            log.error("Error in attempt", e);
                            error.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    error.incrementAndGet();
                }
                return null;
            }));
        }

        // Старт всех потоков
        startLatch.countDown();

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Error ", e);
            }
        }
        executor.shutdown();

        entityManager.clear();
        Doc finalDoc = docRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Документ не найден"));

        ConcurrentApproveResponse response = new ConcurrentApproveResponse();
        response.setTotalAttempts(totalAttempts);
        response.setSuccessCount(success.get());
        response.setConflictCount(conflict.get());
        response.setErrorCount(error.get());
        response.setDoc(DocMapper.mapToDocWithHistoryDto(finalDoc));

        Register register = registerRepository.findByDocId(finalDoc.getId())
                        .orElseThrow(() -> new  EntityNotFoundException("Регистрация не найдена"));
        RegisterDto registerDto = new RegisterDto(register.getId(),register.getMessage(), register.getCreatedDate());
        response.setRegister(registerDto);

        log.info("[DocService]:[CONCURRENT]: гонка потоков успешно завершена, документ id: {} утвержден, регистрация id: {} создана",
                finalDoc.getId(), register.getId());

        return response;
    }

    private <T> List<List<T>> separationToPartitions(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    private User getAuthorOrSystemUser(UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        } else {
            return userRepository.findByUsername("Поросенок Петр")
                    .orElseThrow(() -> new EntityNotFoundException("System User not found"));
        }
    }


}
