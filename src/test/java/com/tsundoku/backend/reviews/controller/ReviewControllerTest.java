package com.tsundoku.backend.reviews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsundoku.backend.auth.entity.Role;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.reviews.dto.CreateReviewRequest;
import com.tsundoku.backend.reviews.dto.ReviewResponse;
import com.tsundoku.backend.reviews.dto.UpdateReviewRequest;
import com.tsundoku.backend.reviews.service.ReviewService;
import com.tsundoku.backend.security.JwtAuthenticationFilter;
import com.tsundoku.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private ReviewResponse sampleReviewResponse;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .email("user@tsundoku.com")
                .password("password")
                .fullName("Test User")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userPrincipal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleReviewResponse = ReviewResponse.builder()
                .id(700L)
                .userBookId(100L)
                .bookTitle("Fahrenheit 451")
                .title("Increíble lectura")
                .content("Una excelente reflexión sobre la libertad.")
                .rating(5)
                .recommend(true)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/library/books/{userBookId}/review debe crear la reseña y retornar 201 Created")
    void createReviewReturnsCreated() throws Exception {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .title("Increíble lectura")
                .content("Una excelente reflexión sobre la libertad.")
                .rating(5)
                .recommend(true)
                .build();

        when(reviewService.createReview(eq(1L), eq(100L), any(CreateReviewRequest.class)))
                .thenReturn(sampleReviewResponse);

        mockMvc.perform(post("/api/v1/library/books/100/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(700L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.bookTitle").value("Fahrenheit 451"));
    }

    @Test
    @DisplayName("GET /api/v1/library/books/{userBookId}/review debe retornar 200 OK con la reseña")
    void getReviewByUserBookReturnsOk() throws Exception {
        when(reviewService.getReviewByUserBook(1L, 100L)).thenReturn(sampleReviewResponse);

        mockMvc.perform(get("/api/v1/library/books/100/review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(700L))
                .andExpect(jsonPath("$.title").value("Increíble lectura"));
    }

    @Test
    @DisplayName("GET /api/v1/reviews/{reviewId} debe retornar 200 OK con la reseña por ID")
    void getReviewByIdReturnsOk() throws Exception {
        when(reviewService.getReviewById(1L, 700L)).thenReturn(sampleReviewResponse);

        mockMvc.perform(get("/api/v1/reviews/700"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(700L));
    }

    @Test
    @DisplayName("PUT /api/v1/reviews/{reviewId} debe actualizar la reseña y retornar 200 OK")
    void updateReviewReturnsOk() throws Exception {
        UpdateReviewRequest request = UpdateReviewRequest.builder()
                .rating(4)
                .content("Actualizando mi opinión")
                .build();

        ReviewResponse updatedResponse = ReviewResponse.builder()
                .id(700L)
                .userBookId(100L)
                .bookTitle("Fahrenheit 451")
                .title("Increíble lectura")
                .content("Actualizando mi opinión")
                .rating(4)
                .recommend(true)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(reviewService.updateReview(eq(1L), eq(700L), any(UpdateReviewRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/reviews/700")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.content").value("Actualizando mi opinión"));
    }

    @Test
    @DisplayName("DELETE /api/v1/reviews/{reviewId} debe eliminar la reseña y retornar 204 No Content")
    void deleteReviewReturnsNoContent() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, 700L);

        mockMvc.perform(delete("/api/v1/reviews/700")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
