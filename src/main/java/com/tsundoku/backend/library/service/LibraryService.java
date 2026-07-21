package com.tsundoku.backend.library.service;

import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.auth.repository.UserRepository;
import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.library.dto.*;
import com.tsundoku.backend.library.entity.Book;
import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.entity.UserBook;
import com.tsundoku.backend.library.mapper.LibraryMapper;
import com.tsundoku.backend.library.repository.BookRepository;
import com.tsundoku.backend.library.repository.UserBookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final LibraryMapper libraryMapper;

    @Transactional
    public UserBookResponse addBookToLibrary(Long userId, AddBookToLibraryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book;
        if (request.bookId() != null) {
            book = bookRepository.findById(request.bookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.bookId()));
        } else if (request.createBook() != null) {
            CreateBookRequest createBook = request.createBook();
            if (createBook.isbn() != null && !createBook.isbn().isBlank()) {
                book = bookRepository.findByIsbn(createBook.isbn().trim())
                        .orElseGet(() -> bookRepository.save(libraryMapper.toBookEntity(createBook)));
            } else {
                book = bookRepository.save(libraryMapper.toBookEntity(createBook));
            }
        } else {
            throw new BadRequestException("Debe proporcionar un bookId existente o los datos para crear un nuevo libro (createBook)");
        }

        if (userBookRepository.existsByUserIdAndBookId(userId, book.getId())) {
            throw new BadRequestException("El libro '" + book.getTitle() + "' ya se encuentra en tu biblioteca personal");
        }

        int currentPage = request.currentPage() != null ? request.currentPage() : 0;
        if (currentPage > book.getPageCount()) {
            throw new BadRequestException("La página actual no puede ser mayor al número total de páginas del libro (" + book.getPageCount() + ")");
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startedAt = null;
        ZonedDateTime finishedAt = null;

        ReadingStatus status = request.status();
        if (status == ReadingStatus.READING || status == ReadingStatus.FINISHED) {
            startedAt = now;
        }
        if (status == ReadingStatus.FINISHED) {
            finishedAt = now;
            currentPage = book.getPageCount();
        }

        UserBook userBook = UserBook.builder()
                .user(user)
                .book(book)
                .status(status)
                .currentPage(currentPage)
                .rating(request.rating())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();

        UserBook savedUserBook = userBookRepository.save(userBook);
        log.info("Libro ID {} añadido exitosamente a la biblioteca del usuario ID {}", book.getId(), userId);

        return libraryMapper.toUserBookResponse(savedUserBook);
    }

    @Transactional(readOnly = true)
    public Page<UserBookResponse> getUserLibrary(Long userId, ReadingStatus status, Pageable pageable) {
        Page<UserBook> userBooks;
        if (status != null) {
            userBooks = userBookRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            userBooks = userBookRepository.findByUserId(userId, pageable);
        }
        return userBooks.map(libraryMapper::toUserBookResponse);
    }

    @Transactional(readOnly = true)
    public UserBookResponse getUserBookById(Long userId, Long userBookId) {
        // OWASP A01: Filtrar explícitamente por id de recurso y ownerId
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro de biblioteca", "id", userBookId));

        return libraryMapper.toUserBookResponse(userBook);
    }

    @Transactional
    public UserBookResponse updateReadingProgress(Long userId, Long userBookId, UpdateReadingProgressRequest request) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro de biblioteca", "id", userBookId));

        int totalPages = userBook.getBook().getPageCount();
        int newPage = request.currentPage();

        if (newPage > totalPages) {
            throw new BadRequestException("La página actual (" + newPage + ") no puede superar el total de páginas (" + totalPages + ")");
        }

        userBook.setCurrentPage(newPage);

        ZonedDateTime now = ZonedDateTime.now();
        if (newPage > 0 && userBook.getStatus() == ReadingStatus.TO_READ) {
            userBook.setStatus(ReadingStatus.READING);
            if (userBook.getStartedAt() == null) {
                userBook.setStartedAt(now);
            }
        }

        if (newPage == totalPages && userBook.getStatus() != ReadingStatus.FINISHED) {
            userBook.setStatus(ReadingStatus.FINISHED);
            userBook.setFinishedAt(now);
        }

        UserBook updated = userBookRepository.save(userBook);
        log.info("Progreso de lectura actualizado a página {} para UserBook ID {}", newPage, userBookId);

        return libraryMapper.toUserBookResponse(updated);
    }

    @Transactional
    public UserBookResponse updateReadingStatus(Long userId, Long userBookId, UpdateReadingStatusRequest request) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro de biblioteca", "id", userBookId));

        ReadingStatus newStatus = request.status();
        ZonedDateTime now = ZonedDateTime.now();

        if (newStatus == ReadingStatus.READING && userBook.getStartedAt() == null) {
            userBook.setStartedAt(now);
        }

        if (newStatus == ReadingStatus.FINISHED) {
            userBook.setFinishedAt(now);
            userBook.setCurrentPage(userBook.getBook().getPageCount());
            if (userBook.getStartedAt() == null) {
                userBook.setStartedAt(now);
            }
        }

        userBook.setStatus(newStatus);
        if (request.rating() != null) {
            userBook.setRating(request.rating());
        }

        UserBook updated = userBookRepository.save(userBook);
        log.info("Estado de lectura actualizado a {} para UserBook ID {}", newStatus, userBookId);

        return libraryMapper.toUserBookResponse(updated);
    }

    @Transactional
    public void removeBookFromLibrary(Long userId, Long userBookId) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro de biblioteca", "id", userBookId));

        userBookRepository.delete(userBook);
        log.info("Libro de biblioteca ID {} eliminado por usuario ID {}", userBookId, userId);
    }
}
