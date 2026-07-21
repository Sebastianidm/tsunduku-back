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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private LibraryMapper libraryMapper = new LibraryMapper();

    @InjectMocks
    private LibraryService libraryService;

    private User sampleUser;
    private Book sampleBook;
    private UserBook sampleUserBook;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("reader@tsundoku.com")
                .fullName("Reading Enthusiast")
                .build();

        sampleBook = Book.builder()
                .id(10L)
                .title("Cien Años de Soledad")
                .author("Gabriel García Márquez")
                .pageCount(400)
                .build();

        sampleUserBook = UserBook.builder()
                .id(100L)
                .user(sampleUser)
                .book(sampleBook)
                .status(ReadingStatus.READING)
                .currentPage(150)
                .build();
    }

    @Test
    @DisplayName("Debe añadir un libro a la biblioteca correctamente")
    void addBookToLibrarySuccess() {
        CreateBookRequest createBookRequest = new CreateBookRequest("9780307474728", "Cien Años de Soledad", "Gabriel García Márquez", "Sudamericana", "1967", "cover.jpg", 400);
        AddBookToLibraryRequest addRequest = new AddBookToLibraryRequest(null, createBookRequest, ReadingStatus.READING, 0, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findByIsbn("9780307474728")).thenReturn(Optional.of(sampleBook));
        when(userBookRepository.existsByUserIdAndBookId(1L, 10L)).thenReturn(false);
        when(userBookRepository.save(any(UserBook.class))).thenReturn(sampleUserBook);

        UserBookResponse response = libraryService.addBookToLibrary(1L, addRequest);

        assertNotNull(response);
        assertEquals("Cien Años de Soledad", response.book().title());
        assertEquals(400, response.totalPages());
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el libro ya está en la biblioteca")
    void addBookToLibraryDuplicateThrowsException() {
        AddBookToLibraryRequest addRequest = new AddBookToLibraryRequest(10L, null, ReadingStatus.TO_READ, 0, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(userBookRepository.existsByUserIdAndBookId(1L, 10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> libraryService.addBookToLibrary(1L, addRequest));
    }

    @Test
    @DisplayName("Debe actualizar el progreso de lectura y calcular porcentaje")
    void updateReadingProgressSuccess() {
        UpdateReadingProgressRequest progressRequest = new UpdateReadingProgressRequest(200);

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse response = libraryService.updateReadingProgress(1L, 100L, progressRequest);

        assertNotNull(response);
        assertEquals(200, response.currentPage());
        assertEquals(50.0, response.progressPercentage());
    }

    @Test
    @DisplayName("Al alcanzar la última página, el estado debe cambiar automáticamente a FINISHED")
    void updateProgressToLastPageChangesStatusToFinished() {
        UpdateReadingProgressRequest progressRequest = new UpdateReadingProgressRequest(400);

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse response = libraryService.updateReadingProgress(1L, 100L, progressRequest);

        assertNotNull(response);
        assertEquals(400, response.currentPage());
        assertEquals(100.0, response.progressPercentage());
        assertEquals(ReadingStatus.FINISHED, response.status());
        assertNotNull(response.finishedAt());
    }

    @Test
    @DisplayName("OWASP A01: Debe fallar si un usuario intenta acceder a un recurso de biblioteca que no le pertenece")
    void getUserBookByIdEnforcesOwnership() {
        when(userBookRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> libraryService.getUserBookById(2L, 100L));
    }
}
