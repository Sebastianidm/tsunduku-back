package com.tsundoku.backend.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsundoku.backend.auth.entity.Role;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.library.dto.*;
import com.tsundoku.backend.library.entity.ReadingStatus;
import com.tsundoku.backend.library.service.LibraryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc(addFilters = false)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private UserBookResponse sampleUserBookResponse;

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

        BookResponse bookResponse = new BookResponse(10L, "9780307474728", "Cien Años de Soledad", "Gabriel García Márquez", "Sudamericana", "1967", "cover.jpg", 400, ZonedDateTime.now());
        sampleUserBookResponse = new UserBookResponse(100L, bookResponse, ReadingStatus.READING, 150, 400, 37.5, 5, ZonedDateTime.now(), null, ZonedDateTime.now(), ZonedDateTime.now());
    }

    @Test
    @DisplayName("GET /api/v1/library/books debe retornar 200 OK con página de libros")
    void getUserLibraryReturnsOk() throws Exception {
        when(libraryService.getUserLibrary(eq(1L), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleUserBookResponse)));

        mockMvc.perform(get("/api/v1/library/books")
                        .principal(() -> "user@tsundoku.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].book.title").value("Cien Años de Soledad"))
                .andExpect(jsonPath("$.content[0].progressPercentage").value(37.5));
    }

    @Test
    @DisplayName("PATCH /api/v1/library/books/{id}/progress debe actualizar la página y retornar 200 OK")
    void updateReadingProgressReturnsOk() throws Exception {
        UpdateReadingProgressRequest request = new UpdateReadingProgressRequest(200);

        when(libraryService.updateReadingProgress(eq(1L), eq(100L), any(UpdateReadingProgressRequest.class)))
                .thenReturn(sampleUserBookResponse);

        mockMvc.perform(patch("/api/v1/library/books/100/progress")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @DisplayName("DELETE /api/v1/library/books/{id} debe eliminar el libro y retornar 204 No Content")
    void removeBookFromLibraryReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/library/books/100")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
