package com.tsundoku.backend.library.repository;

import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.entity.UserBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);

    Optional<UserBook> findByIdAndUserId(Long id, Long userId);

    Page<UserBook> findByUserId(Long userId, Pageable pageable);

    Page<UserBook> findByUserIdAndStatus(Long userId, ReadingStatus status, Pageable pageable);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    long countByUserIdAndStatus(Long userId, ReadingStatus status);
}
