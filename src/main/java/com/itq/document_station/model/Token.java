package com.itq.document_station.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Token() {
    }

    public Token(String refreshToken, User user) {
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
