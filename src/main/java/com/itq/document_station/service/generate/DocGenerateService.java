package com.itq.document_station.service.generate;

import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.model.Doc;
import com.itq.document_station.model.User;
import com.itq.document_station.repository.BatchDocRepository;
import com.itq.document_station.repository.DocRepository;
import com.itq.document_station.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class DocGenerateService {

    @Value("${app.generate:10000}")
    private int count;

    private final UserRepository userRepository;
    private final DocRepository docRepository;
    private final Random random;
    private final Clock clock;

    public DocGenerateService(BatchDocRepository batchDocRepository, UserRepository userRepository) {
        this.docRepository = batchDocRepository;
        this.userRepository = userRepository;
        this.random = new Random();
        this.clock = Clock.systemDefaultZone();
    }

    private List<Doc> generate(int count, User author) {
        final String[] names = { "car", "plane", "house", "yacht" };
        final List<Doc> docs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Doc doc = new Doc();
            doc.setUser(author);
            doc.setName(names[random.nextInt(4)]);
            doc.setDocNumber(UUID.randomUUID().toString());
            docs.add(doc);
        }
        return docs;
    }

    private User getAuthorOrSystemUser(UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        } else {
            return userRepository.findById(5L)
                    .orElseThrow(() -> new EntityNotFoundException("System User not found"));
        }
    }

    @Transactional
    public long createDocs(UserDetails userDetails) {
        long startTime = clock.millis();

        log.info("[GENERATE]: Генерирую {} документ(ов)", count);
        User author = getAuthorOrSystemUser(userDetails);
        List<Doc> docs = generate(count, author);

        log.info("[GENERATE]: Пакетное сохранение...");
        docRepository.customSaveAll(docs);

        long endTime = clock.millis() - startTime;
        log.info("[GENERATE]: Успешно. Сохранено {} документа(ов) за {} ms", count, endTime);
        return endTime;
    }
}

