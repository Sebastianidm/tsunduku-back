package com.tsundoku.backend.library.mapper;

import com.tsundoku.backend.library.dto.BookResponse;
import com.tsundoku.backend.library.dto.CreateBookRequest;
import com.tsundoku.backend.library.dto.UserBookResponse;
import com.tsundoku.backend.library.entity.Book;
import com.tsundoku.backend.library.entity.UserBook;
import org.springframework.stereotype.Component;

@Component
public class LibraryMapper {

    public BookResponse toBookResponse(Book book) {
        if (book == null) {
            return null;
        }
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getCoverUrl(),
                book.getPageCount(),
                book.getCreatedAt()
        );
    }

    public Book toBookEntity(CreateBookRequest request) {
        if (request == null) {
            return null;
        }
        return Book.builder()
                .isbn(request.isbn() != null ? request.isbn().trim() : null)
                .title(request.title().trim())
                .author(request.author().trim())
                .publisher(request.publisher() != null ? request.publisher().trim() : null)
                .publishedDate(request.publishedDate())
                .coverUrl(request.coverUrl())
                .pageCount(request.pageCount())
                .build();
    }

    public UserBookResponse toUserBookResponse(UserBook userBook) {
        if (userBook == null) {
            return null;
        }

        Book book = userBook.getBook();
        BookResponse bookResponse = toBookResponse(book);

        int totalPages = (book != null && book.getPageCount() != null) ? book.getPageCount() : 0;
        int currentPage = userBook.getCurrentPage() != null ? userBook.getCurrentPage() : 0;

        double progressPercentage = 0.0;
        if (totalPages > 0) {
            progressPercentage = Math.min(100.0, Math.round((currentPage * 100.0 / totalPages) * 100.0) / 100.0);
        }

        return new UserBookResponse(
                userBook.getId(),
                bookResponse,
                userBook.getStatus(),
                currentPage,
                totalPages,
                progressPercentage,
                userBook.getRating(),
                userBook.getStartedAt(),
                userBook.getFinishedAt(),
                userBook.getCreatedAt(),
                userBook.getUpdatedAt()
        );
    }
}
