package com.tsundoku.backend.reviews.mapper;

import com.tsundoku.backend.reviews.dto.ReviewResponse;
import com.tsundoku.backend.reviews.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toReviewResponse(Review review) {
        if (review == null) {
            return null;
        }

        String bookTitle = null;
        if (review.getUserBook() != null && review.getUserBook().getBook() != null) {
            bookTitle = review.getUserBook().getBook().getTitle();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .userBookId(review.getUserBook() != null ? review.getUserBook().getId() : null)
                .bookTitle(bookTitle)
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .recommend(review.getRecommend())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
