package com.curapaste.repository;

import com.curapaste.entities.Paste;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasteRepository extends JpaRepository<Paste,Long> {
    Optional<Paste> findByShortId(String shortId);

    boolean existsByShortId(String shortId);

    @Query("""
            SELECT p
            FROM Paste p
            WHERE p.shortId = :shortId
              AND p.deletedAt IS NULL
               AND (
                            p.expiresAt IS NULL
                            OR p.expiresAt > CURRENT_TIMESTAMP
                        )
            """)
    Optional<Paste> findAliveByShortId(
            @Param("shortId") String shortId
    );


    @Transactional
    @Modifying
    @Query("""
            DELETE FROM Paste p
            WHERE p.shortId = :shortId
              AND p.deletedAt IS NULL
            """)
    int deleteAliveByShortId(
            @Param("shortId") String shortId
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE Paste p
            SET p.deletedAt = CURRENT_TIMESTAMP
            WHERE p.shortId = :shortId
              AND p.deletedAt IS NULL
            """)
    int softDelete(
            @Param("shortId") String shortId
    );


    @Query("""
        SELECT p
        FROM Paste p
        WHERE p.expiresAt IS NOT NULL
          AND p.expiresAt <= :now
          AND p.deletedAt IS NULL
        ORDER BY p.expiresAt
        """)
    List<Paste> findExpiredBatch(
            @Param("now") Instant now,
            Pageable pageable
    );


    @Transactional
    @Modifying
    @Query("""
        UPDATE Paste p
        SET p.viewCount = :viewCount,
            p.lastViewedAt = :viewedAt
        WHERE p.shortId = :shortId
          AND p.deletedAt IS NULL
        """)
    int updateAnalytics(
            @Param("shortId") String shortId,
            @Param("viewedAt") Instant viewedAt,
            @Param("viewCount") long viewCount
    );
}
