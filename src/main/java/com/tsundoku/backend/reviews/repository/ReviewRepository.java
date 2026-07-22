package com.tsundoku.backend.reviews.repository;

import com.tsundoku.backend.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserBookId(Long userBookId);

    Optional<Review> findByIdAndUserBookUserId(Long reviewId, Long userId);

    boolean existsByUserBookId(Long userBookId);
}
