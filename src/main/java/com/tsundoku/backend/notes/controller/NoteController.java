package com.tsundoku.backend.notes.controller;

import com.tsundoku.backend.notes.dto.CreateNoteRequest;
import com.tsundoku.backend.notes.dto.NoteResponse;
import com.tsundoku.backend.notes.dto.UpdateNoteRequest;
import com.tsundoku.backend.notes.service.NoteService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Endpoints para tomar notas y registrar citas ancladas a libros")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/library/books/{userBookId}/notes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear una nueva nota o cita anclada", description = "Crea una nota anclada a un número de página de un libro en la biblioteca del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o la página excede el límite del libro"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<NoteResponse> createNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId,
            @Valid @RequestBody CreateNoteRequest request) {

        NoteResponse response = noteService.createNote(userPrincipal.getId(), userBookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/library/books/{userBookId}/notes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener notas de un libro en la biblioteca", description = "Retorna las notas del libro especificado con filtro opcional de número de página y paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notas retornada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado en la biblioteca del usuario")
    })
    public ResponseEntity<Page<NoteResponse>> getNotesByUserBook(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userBookId,
            @RequestParam(required = false) Integer pageNumber,
            @PageableDefault(size = 20, sort = "pageNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<NoteResponse> page = noteService.getNotesByUserBook(userPrincipal.getId(), userBookId, pageNumber, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/notes/{noteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalle de una nota", description = "Retorna una nota específica por su ID si pertenece al usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota retornada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada o no pertenece al usuario")
    })
    public ResponseEntity<NoteResponse> getNoteById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long noteId) {

        NoteResponse response = noteService.getNoteById(userPrincipal.getId(), noteId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notes/{noteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar una nota existente", description = "Modifica los campos especificados de una nota perteneciente al usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o la página excede el límite del libro"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada o no pertenece al usuario")
    })
    public ResponseEntity<NoteResponse> updateNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request) {

        NoteResponse response = noteService.updateNote(userPrincipal.getId(), noteId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notes/{noteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar una nota", description = "Elimina permanentemente una nota perteneciente al usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada o no pertenece al usuario")
    })
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long noteId) {

        noteService.deleteNote(userPrincipal.getId(), noteId);
        return ResponseEntity.noContent().build();
    }
}
