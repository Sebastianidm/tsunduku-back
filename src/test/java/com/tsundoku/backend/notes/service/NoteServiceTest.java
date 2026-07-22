package com.tsundoku.backend.notes.service;

import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.library.entity.Book;
import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.entity.UserBook;
import com.tsundoku.backend.library.repository.UserBookRepository;
import com.tsundoku.backend.notes.dto.CreateNoteRequest;
import com.tsundoku.backend.notes.dto.NoteResponse;
import com.tsundoku.backend.notes.dto.UpdateNoteRequest;
import com.tsundoku.backend.notes.entity.Note;
import com.tsundoku.backend.notes.mapper.NoteMapper;
import com.tsundoku.backend.notes.repository.NoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Spy
    private NoteMapper noteMapper = new NoteMapper();

    @InjectMocks
    private NoteService noteService;

    private User sampleUser;
    private Book sampleBook;
    private UserBook sampleUserBook;
    private Note sampleNote;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("reader@tsundoku.com")
                .fullName("Reading Enthusiast")
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
                .status(ReadingStatus.READING)
                .currentPage(42)
                .build();

        sampleNote = Note.builder()
                .id(500L)
                .userBook(sampleUserBook)
                .pageNumber(42)
                .content("La importancia de la lectura en una sociedad distópica.")
                .quote("Había un placer especial en ver arder las cosas...")
                .isSpoiler(false)
                .tags("reflexión, distopía")
                .build();
    }

    @Test
    @DisplayName("Debe crear una nota correctamente anclada a una página")
    void createNoteSuccess() {
        CreateNoteRequest request = CreateNoteRequest.builder()
                .pageNumber(42)
                .content("La importancia de la lectura en una sociedad distópica.")
                .quote("Había un placer especial en ver arder las cosas...")
                .isSpoiler(false)
                .tags("reflexión, distopía")
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);

        NoteResponse response = noteService.createNote(1L, 100L, request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(100L, response.getUserBookId());
        assertEquals(42, response.getPageNumber());
        assertEquals("La importancia de la lectura en una sociedad distópica.", response.getContent());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el número de página excede el total de páginas del libro")
    void createNotePageExceedsTotalPagesThrowsException() {
        CreateNoteRequest request = CreateNoteRequest.builder()
                .pageNumber(300) // Libro tiene 249 páginas
                .content("Nota fuera de rango")
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.createNote(1L, 100L, request));

        assertTrue(exception.getMessage().contains("cannot exceed total pages"));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("OWASP A01: Debe fallar si el libro de la biblioteca no pertenece al usuario")
    void createNoteUserBookNotFoundThrowsException() {
        CreateNoteRequest request = CreateNoteRequest.builder()
                .pageNumber(10)
                .content("Nota sin permisos")
                .build();

        when(userBookRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noteService.createNote(2L, 100L, request));
    }

    @Test
    @DisplayName("Debe listar notas de un libro con paginación")
    void getNotesByUserBookSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Note> page = new PageImpl<>(List.of(sampleNote), pageable, 1);

        when(userBookRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleUserBook));
        when(noteRepository.findByUserBookId(100L, pageable)).thenReturn(page);

        Page<NoteResponse> response = noteService.getNotesByUserBook(1L, 100L, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("La importancia de la lectura en una sociedad distópica.", response.getContent().get(0).getContent());
    }

    @Test
    @DisplayName("Debe obtener una nota por ID si pertenece al usuario autenticado")
    void getNoteByIdSuccess() {
        when(noteRepository.findByIdAndUserBookUserId(500L, 1L)).thenReturn(Optional.of(sampleNote));

        NoteResponse response = noteService.getNoteById(1L, 500L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
    }

    @Test
    @DisplayName("Debe actualizar una nota correctamente")
    void updateNoteSuccess() {
        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .content("Contenido actualizado")
                .isSpoiler(true)
                .build();

        when(noteRepository.findByIdAndUserBookUserId(500L, 1L)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        NoteResponse response = noteService.updateNote(1L, 500L, request);

        assertNotNull(response);
        assertEquals("Contenido actualizado", response.getContent());
        assertTrue(response.getIsSpoiler());
    }

    @Test
    @DisplayName("Debe eliminar una nota si pertenece al usuario")
    void deleteNoteSuccess() {
        when(noteRepository.findByIdAndUserBookUserId(500L, 1L)).thenReturn(Optional.of(sampleNote));

        noteService.deleteNote(1L, 500L);

        verify(noteRepository, times(1)).delete(sampleNote);
    }
}
