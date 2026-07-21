package com.tsundoku.backend.library.controller;

import com.tsundoku.backend.library.dto.*;
import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.service.LibraryService;
import com.tsundoku.backend.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
@Tag(name = "Library", description = "Endpoints para la gestión de la biblioteca personal y progreso de lectura")
@SecurityRequirement(name = "bearerAuth")
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping("/books")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Agregar un libro a la biblioteca personal", description = "Asocia un libro a la biblioteca del usuario con su estado y progreso inicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Libro añadido exitosamente a la biblioteca"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el libro ya está en la biblioteca"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<UserBookResponse> addBookToLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddBookToLibraryRequest request) {

        UserBookResponse response = libraryService.addBookToLibrary(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/books")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener libros de la biblioteca personal", description = "Retorna la lista de libros del usuario autenticado con filtros opcionales de estado y paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de libros retornada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Page<UserBookResponse>> getUserLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) ReadingStatus status,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserBookResponse> page = libraryService.getUserLibrary(userPrincipal.getId(), status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/books/{userBookId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalle de un libro en la biblioteca", description = "Retorna la información y progreso de un libro específico por ID en la biblioteca del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle del libro retornado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<UserBookResponse> getUserBookById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId) {

        UserBookResponse response = libraryService.getUserBookById(userPrincipal.getId(), userBookId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/books/{userBookId}/progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar progreso de lectura", description = "Actualiza la página actual de lectura de un libro en la biblioteca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progreso actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Página actual excede el total de páginas del libro"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<UserBookResponse> updateReadingProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId,
            @Valid @RequestBody UpdateReadingProgressRequest request) {

        UserBookResponse response = libraryService.updateReadingProgress(userPrincipal.getId(), userBookId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/books/{userBookId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cambiar estado de lectura o rating", description = "Actualiza el estado (ej. LEYENDO -> TERMINADO) y/o calificación de un libro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de estado inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<UserBookResponse> updateReadingStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId,
            @Valid @RequestBody UpdateReadingStatusRequest request) {

        UserBookResponse response = libraryService.updateReadingStatus(userPrincipal.getId(), userBookId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{userBookId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar libro de la biblioteca personal", description = "Elimina un libro de la biblioteca personal del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Libro eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<Void> removeBookFromLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId) {

        libraryService.removeBookFromLibrary(userPrincipal.getId(), userBookId);
        return ResponseEntity.noContent().build();
    }
}
