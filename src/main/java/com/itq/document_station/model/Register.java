package com.itq.document_station.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Реестр утверждений
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "register")
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "register_seq")
    @SequenceGenerator(name = "register_seq", sequenceName = "register_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Сообщение о записи в реестр
     */
    @Column(name = "message")
    private String message;

    /**
     * Дата утверждения в реестре
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Документ, который утвержлили
     */
    @OneToOne
    @JoinColumn(name = "doc_id", unique = true, nullable = false)
    private Doc doc;

}
