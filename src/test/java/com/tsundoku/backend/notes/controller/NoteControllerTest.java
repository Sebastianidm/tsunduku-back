package com.tsundoku.backend.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsundoku.backend.auth.entity.Role;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.notes.dto.CreateNoteRequest;
import com.tsundoku.backend.notes.dto.NoteResponse;
import com.tsundoku.backend.notes.dto.UpdateNoteRequest;
import com.tsundoku.backend.notes.service.NoteService;
import com.tsundoku.backend.security.JwtAuthenticationFilter;
import com.tsundoku.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private NoteResponse sampleNoteResponse;

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

        sampleNoteResponse = NoteResponse.builder()
                .id(500L)
                .userBookId(100L)
                .pageNumber(42)
                .content("Reflexión sobre el capítulo 5")
                .quote("Una cita memorable")
                .isSpoiler(false)
                .tags("reflexion, personaje")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/library/books/{userBookId}/notes debe crear la nota y retornar 201 Created")
    void createNoteReturnsCreated() throws Exception {
        CreateNoteRequest request = CreateNoteRequest.builder()
                .pageNumber(42)
                .content("Reflexión sobre el capítulo 5")
                .quote("Una cita memorable")
                .isSpoiler(false)
                .tags("reflexion, personaje")
                .build();

        when(noteService.createNote(eq(1L), eq(100L), any(CreateNoteRequest.class)))
                .thenReturn(sampleNoteResponse);

        mockMvc.perform(post("/api/v1/library/books/100/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.pageNumber").value(42))
                .andExpect(jsonPath("$.content").value("Reflexión sobre el capítulo 5"));
    }

    @Test
    @DisplayName("GET /api/v1/library/books/{userBookId}/notes debe retornar 200 OK con las notas del libro")
    void getNotesByUserBookReturnsOk() throws Exception {
        when(noteService.getNotesByUserBook(eq(1L), eq(100L), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleNoteResponse)));

        mockMvc.perform(get("/api/v1/library/books/100/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(500L))
                .andExpect(jsonPath("$.content[0].pageNumber").value(42));
    }

    @Test
    @DisplayName("GET /api/v1/notes/{noteId} debe retornar 200 OK con el detalle de la nota")
    void getNoteByIdReturnsOk() throws Exception {
        when(noteService.getNoteById(1L, 500L)).thenReturn(sampleNoteResponse);

        mockMvc.perform(get("/api/v1/notes/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.tags").value("reflexion, personaje"));
    }

    @Test
    @DisplayName("PUT /api/v1/notes/{noteId} debe actualizar la nota y retornar 200 OK")
    void updateNoteReturnsOk() throws Exception {
        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .content("Contenido editado")
                .isSpoiler(true)
                .build();

        NoteResponse updatedResponse = NoteResponse.builder()
                .id(500L)
                .userBookId(100L)
                .pageNumber(42)
                .content("Contenido editado")
                .quote("Una cita memorable")
                .isSpoiler(true)
                .tags("reflexion, personaje")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(noteService.updateNote(eq(1L), eq(500L), any(UpdateNoteRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/notes/500")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.content").value("Contenido editado"))
                .andExpect(jsonPath("$.isSpoiler").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/notes/{noteId} debe eliminar la nota y retornar 204 No Content")
    void deleteNoteReturnsNoContent() throws Exception {
        doNothing().when(noteService).deleteNote(1L, 500L);

        mockMvc.perform(delete("/api/v1/notes/500")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
