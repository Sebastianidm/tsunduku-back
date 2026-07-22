package com.tsundoku.backend.reviews.service;

import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.library.entity.Book;
import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.entity.UserBook;
import com.tsundoku.backend.library.repository.UserBookRepository;
import com.tsundoku.backend.reviews.dto.CreateReviewRequest;
import com.tsundoku.backend.reviews.dto.ReviewResponse;
import com.tsundoku.backend.reviews.dto.UpdateReviewRequest;
import com.tsundoku.backend.reviews.entity.Review;
import com.tsundoku.backend.reviews.mapper.ReviewMapper;
import com.tsundoku.backend.reviews.repository.ReviewRepository;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapper();

    @InjectMocks
    private ReviewService reviewService;

    private User sampleUser;
    private Book sampleBook;
    private UserBook sampleUserBook;
    private Review sampleReview;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("reviewer@tsundoku.com")
                .fullName("Reviewer User")
                .build();

        sampleBook = Book.builder()
                .id(10L)
                .title("Fahrenheit 451")
                .author("Ray Bradbury")
                .pageCount(249)
                .build();

        sampleUserBook = UserBook.builder()
                .id(100L)
                .user(sampleUser)
                .book(sampleBook)
                .status(ReadingStatus.FINISHED)
                .currentPage(249)
                .rating(null)
                .build();

        sampleReview = Review.builder()
                .id(700L)
                .userBook(sampleUserBook)
                .title("Increíble distopía sobre la censura")
                .content("Un libro fascinante sobre el valor de la lectura y la libertad de pensamiento.")
                .rating(5)
                .recommend(true)
                .build();
    }

    @Test
    @DisplayName("Debe crear una reseña correctamente y sincronizar el rating del UserBook")
    void createReviewSuccess() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .title("Increíble distopía sobre la censura")
                .content("Un libro fascinante sobre el valor de la lectura y la libertad de pensamiento.")
                .rating(5)
                .recommend(true)
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(reviewRepository.existsByUserBookId(100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);

        ReviewResponse response = reviewService.createReview(1L, 100L, request);

        assertNotNull(response);
        assertEquals(700L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("Fahrenheit 451", response.getBookTitle());
        assertEquals(5, sampleUserBook.getRating()); // Verifica que sincronizó el rating en el UserBook
        verify(userBookRepository, times(1)).save(sampleUserBook);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el libro ya tiene una reseña creada")
    void createReviewDuplicateThrowsBadRequestException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .title("Reseña duplicada")
                .content("Contenido adicional")
                .rating(4)
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(reviewRepository.existsByUserBookId(100L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> reviewService.createReview(1L, 100L, request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("OWASP A01: Debe fallar si el usuario intenta reseñar un libro que no le pertenece")
    void createReviewUserBookNotFoundThrowsResourceNotFoundException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .title("Reseña ajena")
                .content("Sin permisos")
                .rating(3)
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(2L, 100L, request));
    }

    @Test
    @DisplayName("Debe obtener la reseña de un libro por su UserBook ID")
    void getReviewByUserBookSuccess() {
        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(reviewRepository.findByUserBookId(100L)).thenReturn(Optional.of(sampleReview));

        ReviewResponse response = reviewService.getReviewByUserBook(1L, 100L);

        assertNotNull(response);
        assertEquals(700L, response.getId());
        assertEquals("Increíble distopía sobre la censura", response.getTitle());
    }

    @Test
    @DisplayName("Debe actualizar una reseña y sincronizar el rating")
    void updateReviewSuccess() {
        UpdateReviewRequest request = UpdateReviewRequest.builder()
                .rating(4)
                .content("Contenido editado")
                .build();

        when(reviewRepository.findByIdAndUserBookUserId(700L, 1L)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = reviewService.updateReview(1L, 700L, request);

        assertNotNull(response);
        assertEquals(4, response.getRating());
        assertEquals("Contenido editado", response.getContent());
        assertEquals(4, sampleUserBook.getRating());
    }

    @Test
    @DisplayName("Debe eliminar una reseña existente si pertenece al usuario")
    void deleteReviewSuccess() {
        when(reviewRepository.findByIdAndUserBookUserId(700L, 1L)).thenReturn(Optional.of(sampleReview));

        reviewService.deleteReview(1L, 700L);

        verify(reviewRepository, times(1)).delete(sampleReview);
    }
}
