package com.itq.document_station.repository;

import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.model.Doc;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.itq.document_station.enumeration.StatusEnum.DRAFT;

@Repository
public class BatchDocRepository implements DocRepository {

    private final JdbcTemplate jdbcTemplate;

    public BatchDocRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void customSaveAll(List<Doc> docs) {
        jdbcTemplate.batchUpdate("INSERT INTO DOC (DOC_NUMBER, NAME, USER_ID, STATUS, CREATED_DATE, UPDATED_DATE) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                docs,
                100,
                (PreparedStatement ps, Doc doc) -> {
                    ps.setString(1, doc.getDocNumber());
                    ps.setString(2, doc.getName());
                    ps.setLong(3, doc.getUser().getId());
                    ps.setString(4, DRAFT.name());
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                });
    }

    @Override
    public Optional<Doc> findByIdWithHistory(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Doc> findAllByIdWithLock(List<Long> ids) {
        return List.of();
    }

    @Override
    public List<Doc> findByStatusOrderByIdAsc(StatusEnum status, Pageable pageable) {
        return List.of();
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Doc> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Doc> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<Doc> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Doc getOne(Long aLong) {
        return null;
    }

    @Override
    public Doc getById(Long aLong) {
        return null;
    }

    @Override
    public Doc getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Doc> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Doc> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Doc> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Doc> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Doc> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Doc> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Doc, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public Optional<Doc> findOne(Specification<Doc> spec) {
        return Optional.empty();
    }

    @Override
    public List<Doc> findAll(Specification<Doc> spec) {
        return List.of();
    }

    @Override
    public Page<Doc> findAll(Specification<Doc> spec, Pageable pageable) {
        return null;
    }

    @Override
    public List<Doc> findAll(Specification<Doc> spec, Sort sort) {
        return List.of();
    }

    @Override
    public long count(Specification<Doc> spec) {
        return 0;
    }

    @Override
    public boolean exists(Specification<Doc> spec) {
        return false;
    }

    @Override
    public long delete(Specification<Doc> spec) {
        return 0;
    }

    @Override
    public <S extends Doc, R> R findBy(Specification<Doc> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Doc> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Doc> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<Doc> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<Doc> findAll() {
        return List.of();
    }

    @Override
    public List<Doc> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Doc entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Doc> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Doc> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Doc> findAll(Pageable pageable) {
        return null;
    }
}
