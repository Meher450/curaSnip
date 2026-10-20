package com.curapaste.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pastes")
@Builder
public class Paste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;


    @Column(name = "short_id", nullable = false, unique = true, length = 12)
    private String shortId;


    private Instant createdAt;



    @Column(length = 512)
    private String contentLocation;

    @Column(nullable = false)
    private Integer sizeBytes;


    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "burn_after_read", nullable = false)
    private boolean burnAfterRead;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "delete_token_hash", nullable = false, length = 255)
    private String deleteTokenHash;

    @Column(name = "deleted_at")
    private Instant deletedAt;


    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "last_viewed_at")
    private Instant lastViewedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

}
