package com.itq.document_station.model;

import com.itq.document_station.enumeration.ActionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * История действий в документе
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(name = "history_seq", sequenceName = "history_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Автор
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Действие над документом
     */
    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionEnum action;

    /**
     * Дата создания
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Комментарий
     */
    @Column(name = "comment")
    private String comment;

    /**
     * Документ
     */
    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private Doc doc;

}
