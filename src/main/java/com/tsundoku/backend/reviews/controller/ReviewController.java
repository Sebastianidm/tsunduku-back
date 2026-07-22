package com.tsundoku.backend.reviews.controller;

import com.tsundoku.backend.reviews.dto.CreateReviewRequest;
import com.tsundoku.backend.reviews.dto.ReviewResponse;
import com.tsundoku.backend.reviews.dto.UpdateReviewRequest;
import com.tsundoku.backend.reviews.service.ReviewService;
import com.tsundoku.backend.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Endpoints para la gestión de reseñas y valoraciones de libros")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/library/books/{userBookId}/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear reseña y valoración para un libro", description = "Asocia una reseña final y rating (1-5 estrellas) a un libro de la biblioteca personal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ya existe una reseña para este libro"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.createReview(userPrincipal.getId(), userBookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/library/books/{userBookId}/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener reseña de un libro en la biblioteca", description = "Retorna la reseña y calificación asociada a un libro específico del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña retornada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro o reseña no encontrada")
    })
    public ResponseEntity<ReviewResponse> getReviewByUserBook(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId) {

        ReviewResponse response = reviewService.getReviewByUserBook(userPrincipal.getId(), userBookId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalle de una reseña por ID", description = "Retorna los datos de una reseña si pertenece al usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña retornada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada o sin permisos")
    })
    public ResponseEntity<ReviewResponse> getReviewById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reviewId) {

        ReviewResponse response = reviewService.getReviewById(userPrincipal.getId(), reviewId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar una reseña existente", description = "Modifica los campos especificados de una reseña del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada o sin permisos")
    })
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        ReviewResponse response = reviewService.updateReview(userPrincipal.getId(), reviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar una reseña", description = "Elimina permanentemente una reseña de la biblioteca del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reseña eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada o sin permisos")
    })
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(userPrincipal.getId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
