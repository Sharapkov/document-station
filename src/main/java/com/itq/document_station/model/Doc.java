package com.itq.document_station.model;

import com.itq.document_station.enumeration.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Документ
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "doc")
public class Doc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(name = "history_seq", sequenceName = "history_id_seq", allocationSize = 50)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Уникальный номер документа uuid
     */
    @Column(name = "doc_number", nullable = false, unique = true)
    private String docNumber;

    /**
     * Автор документа
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Название документа
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Статус документа
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    /**
     * Дата создания
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Дата обновления
     */
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    /**
     * История изменений
     */
    @OneToMany(mappedBy = "doc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<History> histories = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (docNumber == null) {
            docNumber = UUID.randomUUID().toString();
        }
    }

}
