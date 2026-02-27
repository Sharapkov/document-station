package com.itq.document_station;

import com.itq.document_station.config.AppConfig;
import com.itq.document_station.dto.DocCreateRequest;
import com.itq.document_station.dto.DocDto;
import com.itq.document_station.dto.DocWithHistoryDto;
import com.itq.document_station.enumeration.ActionEnum;
import com.itq.document_station.enumeration.ProcessingResult;
import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.model.Doc;
import com.itq.document_station.model.Register;
import com.itq.document_station.repository.DocRepository;
import com.itq.document_station.repository.RegisterRepository;
import com.itq.document_station.repository.UserRepository;
import com.itq.document_station.service.impl.DocServiceImpl;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DocServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";
    private static final String DOC_NAME = "Test Document";

    @Mock private UserRepository userRepository;
    @Mock private DocRepository docRepository;
    @Mock private RegisterRepository registerRepository;
    @Mock private EntityManager entityManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private AppConfig appConfig;

    private DocServiceImpl docService;
    private com.itq.document_station.model.User author;

    @BeforeEach
    void setUp() {
        author = new com.itq.document_station.model.User();
        author.setId(1L);
        author.setUsername(TEST_USERNAME);

        Executor syncExecutor = Runnable::run;
        docService = new DocServiceImpl(
                userRepository,
                docRepository,
                registerRepository,
                entityManager,
                transactionTemplate,
                syncExecutor,   // вместо taskSubmitExecutor
                syncExecutor,   // вместо taskApproveExecutor
                appConfig
        );
    }

    private UserDetails createUserDetails() {
        return new User(TEST_USERNAME, TEST_PASSWORD, Collections.emptyList());
    }

    private DocCreateRequest createDocRequest(String name) {
        DocCreateRequest request = new DocCreateRequest();
        request.setName(name);
        return request;
    }

    private Doc createDoc(Long id, StatusEnum status) {
        Doc doc = new Doc();
        doc.setId(id);
        doc.setUser(author);
        doc.setName(DOC_NAME);
        doc.setStatus(status);
        doc.setCreatedDate(LocalDateTime.now());
        doc.setUpdatedDate(LocalDateTime.now());
        return doc;
    }

    @Test
    @DisplayName("createDoc: успешное создание документа")
    void createDoc() {
        // given
        DocCreateRequest request = createDocRequest(DOC_NAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(author));

        // when
        DocDto result = docService.createDoc(createUserDetails(), request);

        // then
        ArgumentCaptor<Doc> docCaptor = ArgumentCaptor.forClass(Doc.class);
        verify(docRepository).save(docCaptor.capture());
        Doc savedDoc = docCaptor.getValue();

        assertThat(savedDoc)
                .returns(DOC_NAME, Doc::getName)
                .returns(StatusEnum.DRAFT, Doc::getStatus)
                .satisfies(doc -> assertThat(doc.getUser()).isEqualTo(author));

        assertThat(result)
                .isNotNull()
                .returns(DOC_NAME, DocDto::getName)
                .returns(StatusEnum.DRAFT, DocDto::getStatus)
                .satisfies(dto -> assertThat(dto.getUser().getId()).isEqualTo(author.getId()));
    }

    @Test
    @DisplayName("findById: документ найден → возвращает DocWithHistoryDto")
    void findById() {
        // given
        long docId = 1L;
        Doc doc = createDoc(docId, StatusEnum.DRAFT);

        when(docRepository.findByIdWithHistory(docId)).thenReturn(Optional.of(doc));

        // when
        DocWithHistoryDto result = docService.findById(docId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(docId);
        verify(docRepository).findByIdWithHistory(docId);
    }

    @Test
    @DisplayName("submitDocuments: все документы в DRAFT → SUCCESS для всех")
    void submitDocumentsAllSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L);
        String comment = "submit";


        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(null);
                });
        when(appConfig.getPARTITION_SIZE()).thenReturn(10);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(author));
        when(docRepository.findById(1L)).thenReturn(Optional.of(createDoc(1L, StatusEnum.DRAFT)));
        when(docRepository.findById(2L)).thenReturn(Optional.of(createDoc(2L, StatusEnum.DRAFT)));

        // when
        Map<Long, ProcessingResult> result = docService.submitDocuments(ids, createUserDetails(), comment);

        // then
        assertThat(result)
                .hasSize(2)
                .containsEntry(1L, ProcessingResult.SUCCESS)
                .containsEntry(2L, ProcessingResult.SUCCESS);

        verify(docRepository, times(2)).save(any(Doc.class));

        ArgumentCaptor<Doc> docCaptor = ArgumentCaptor.forClass(Doc.class);
        verify(docRepository, atLeastOnce()).save(docCaptor.capture());
        docCaptor.getAllValues().forEach(doc -> {
            assertThat(doc.getStatus()).isEqualTo(StatusEnum.SUBMITTED);
            assertThat(doc.getHistories())
                    .hasSize(1)
                    .first()
                    .satisfies(history -> {
                        assertThat(history.getAction()).isEqualTo(ActionEnum.SHIFT_SUBMITTED);
                        assertThat(history.getComment()).isEqualTo(comment);
                        assertThat(history.getUser()).isEqualTo(author);
                    });
        });
    }

    @Test
    @DisplayName("submitDocuments: частичный успех")
    void submitDocumentsNotFound() {
        // given
        List<Long> ids = List.of(1L, 2L);
        String comment = "submit";

        // Мок TransactionStatus, который ничего не делает при вызове setRollbackOnly
        TransactionStatus txStatus = mock(TransactionStatus.class);
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(txStatus);
                });
        when(appConfig.getPARTITION_SIZE()).thenReturn(10);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(author));
        when(docRepository.findById(1L)).thenReturn(Optional.of(createDoc(1L, StatusEnum.DRAFT)));
        when(docRepository.findById(2L)).thenReturn(Optional.empty());

        // when
        Map<Long, ProcessingResult> result = docService.submitDocuments(ids, createUserDetails(), comment);

        // then
        assertThat(result)
                .hasSize(2)
                .containsEntry(1L, ProcessingResult.SUCCESS)
                .containsEntry(2L, ProcessingResult.NOT_FOUND);

        // только первый сохранён
        verify(docRepository, times(1)).save(any(Doc.class));
    }

    @Test
    @DisplayName("approveDocuments: частичный успех + создание Register")
    void approveDocumentsAllSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L);
        String comment = "approve";

        // Мок TransactionStatus, который ничего не делает при вызове setRollbackOnly
        TransactionStatus txStatus = mock(TransactionStatus.class);
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(txStatus);
                });
        when(appConfig.getPARTITION_SIZE()).thenReturn(10);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(author));
        when(docRepository.findById(1L)).thenReturn(Optional.of(createDoc(1L, StatusEnum.SUBMITTED)));
        when(docRepository.findById(2L)).thenReturn(Optional.of(createDoc(2L, StatusEnum.DRAFT)));

        // when
        Map<Long, ProcessingResult> result = docService.approveDocuments(ids, createUserDetails(), comment);

        // then
        assertThat(result)
                .hasSize(2)
                .containsEntry(1L, ProcessingResult.SUCCESS)
                .containsEntry(2L, ProcessingResult.CONFLICT);

        verify(docRepository, times(1)).save(any(Doc.class));
        verify(registerRepository, times(1)).save(any(Register.class));

        ArgumentCaptor<Register> regCaptor = ArgumentCaptor.forClass(Register.class);
        verify(registerRepository, atLeastOnce()).save(regCaptor.capture());
        regCaptor.getAllValues().forEach(reg -> {
            assertThat(reg.getMessage()).contains("утвержден");
            assertThat(reg.getDoc()).isNotNull();
        });
    }


    @Test
    @DisplayName("approveDocuments: ошибка при сохранении в Register → CONFLICT, транзакция откатывается")
    void approveDocumentsConflict() {
        // given
        List<Long> ids = List.of(1L);
        String comment = "approve";

        // Создаём мок TransactionStatus
        TransactionStatus txStatus = mock(TransactionStatus.class);
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(txStatus);
                });

        when(appConfig.getPARTITION_SIZE()).thenReturn(10);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(author));

        Doc doc = createDoc(1L, StatusEnum.SUBMITTED);
        when(docRepository.findById(1L)).thenReturn(Optional.of(doc));

        doThrow(new RuntimeException("Незивестная ошибка, откатываемся ")).when(registerRepository).save(any(Register.class));

        // when
        Map<Long, ProcessingResult> result = docService.approveDocuments(ids, createUserDetails(), comment);

        // then
        assertThat(result)
                .hasSize(1)
                .containsEntry(1L, ProcessingResult.CONFLICT);

        verify(docRepository, times(1)).save(any(Doc.class));
        verify(registerRepository, times(1)).save(any(Register.class));

        // транзакция помечена на откат?
        verify(txStatus).setRollbackOnly();
    }

}



