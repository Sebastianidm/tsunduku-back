package com.tsundoku.backend.reviews.service;

import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.library.entity.UserBook;
import com.tsundoku.backend.library.repository.UserBookRepository;
import com.tsundoku.backend.reviews.dto.CreateReviewRequest;
import com.tsundoku.backend.reviews.dto.ReviewResponse;
import com.tsundoku.backend.reviews.dto.UpdateReviewRequest;
import com.tsundoku.backend.reviews.entity.Review;
import com.tsundoku.backend.reviews.mapper.ReviewMapper;
import com.tsundoku.backend.reviews.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserBookRepository userBookRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(Long userId, Long userBookId, CreateReviewRequest request) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserBook", "id", userBookId));

        if (reviewRepository.existsByUserBookId(userBookId)) {
            throw new BadRequestException("A review already exists for this book in your library. Use the update endpoint to modify it.");
        }

        Review review = Review.builder()
                .userBook(userBook)
                .title(request.getTitle())
                .content(request.getContent())
                .rating(request.getRating())
                .recommend(Boolean.TRUE.equals(request.getRecommend()))
                .build();

        Review savedReview = reviewRepository.save(review);

        userBook.setRating(request.getRating());
        userBookRepository.save(userBook);

        return reviewMapper.toReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByUserBook(Long userId, Long userBookId) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserBook", "id", userBookId));

        Review review = reviewRepository.findByUserBookId(userBook.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "userBookId", userBookId));

        return reviewMapper.toReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdAndUserBookUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        return reviewMapper.toReviewResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByIdAndUserBookUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
            review.getUserBook().setRating(request.getRating());
            userBookRepository.save(review.getUserBook());
        }

        if (request.getRecommend() != null) {
            review.setRecommend(request.getRecommend());
        }

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdAndUserBookUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        reviewRepository.delete(review);
    }
}
