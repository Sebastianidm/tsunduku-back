package com.tsundoku.backend.library.dto;

import com.tsundoku.backend.library.entity.ReadingStatus;

import java.time.ZonedDateTime;

public record UserBookResponse(
        Long id,
        BookResponse book,
        ReadingStatus status,
        Integer currentPage,
        Integer totalPages,
        double progressPercentage,
        Integer rating,
        ZonedDateTime startedAt,
        ZonedDateTime finishedAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {}
