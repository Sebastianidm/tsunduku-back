package com.tsundoku.backend.library.dto;

import java.time.ZonedDateTime;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        String publishedDate,
        String coverUrl,
        Integer pageCount,
        ZonedDateTime createdAt
) {}
