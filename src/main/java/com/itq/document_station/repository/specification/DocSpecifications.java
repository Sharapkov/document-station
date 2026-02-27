package com.itq.document_station.repository.specification;

import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.model.Doc;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class DocSpecifications {

    public static Specification<Doc> idIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("id").in(ids);
        };
    }

    public static Specification<Doc> hasStatus(StatusEnum status) {

        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Doc> hasAuthorId(Long authorId) {
        return (root, query, cb) -> {
            if (authorId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user").get("id"), authorId);
        };
    }

    public static Specification<Doc> byDate(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("updatedDate"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("updatedDate"), from);
            }
            return cb.lessThanOrEqualTo(root.get("updatedDate"), to);
        };
    }
}
