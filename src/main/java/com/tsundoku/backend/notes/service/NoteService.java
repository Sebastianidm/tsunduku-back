package com.tsundoku.backend.notes.service;

import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.library.entity.UserBook;
import com.tsundoku.backend.library.repository.UserBookRepository;
import com.tsundoku.backend.notes.dto.CreateNoteRequest;
import com.tsundoku.backend.notes.dto.NoteResponse;
import com.tsundoku.backend.notes.dto.UpdateNoteRequest;
import com.tsundoku.backend.notes.entity.Note;
import com.tsundoku.backend.notes.mapper.NoteMapper;
import com.tsundoku.backend.notes.repository.NoteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserBookRepository userBookRepository;
    private final NoteMapper noteMapper;

    @Transactional
    public NoteResponse createNote(Long userId, Long userBookId, CreateNoteRequest request) {
        UserBook userBook = findUserBookAndVerifyOwner(userBookId, userId);

        validatePageNumber(request.getPageNumber(), userBook);

        Note note = Note.builder()
                .userBook(userBook)
                .pageNumber(request.getPageNumber())
                .content(request.getContent())
                .quote(request.getQuote())
                .isSpoiler(Boolean.TRUE.equals(request.getIsSpoiler()))
                .tags(request.getTags())
                .build();

        Note savedNote = noteRepository.save(note);
        return noteMapper.toNoteResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotesByUserBook(Long userId, Long userBookId, Integer pageNumber, Pageable pageable) {
        findUserBookAndVerifyOwner(userBookId, userId);

        Page<Note> notes;
        if (pageNumber != null) {
            notes = noteRepository.findByUserBookIdAndPageNumber(userBookId, pageNumber, pageable);
        } else {
            notes = noteRepository.findByUserBookId(userBookId, pageable);
        }

        return notes.map(noteMapper::toNoteResponse);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserBookUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        return noteMapper.toNoteResponse(note);
    }

    @Transactional
    public NoteResponse updateNote(Long userId, Long noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserBookUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (request.getPageNumber() != null) {
            validatePageNumber(request.getPageNumber(), note.getUserBook());
            note.setPageNumber(request.getPageNumber());
        }

        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        if (request.getQuote() != null) {
            note.setQuote(request.getQuote());
        }

        if (request.getIsSpoiler() != null) {
            note.setIsSpoiler(request.getIsSpoiler());
        }

        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }

        Note updatedNote = noteRepository.save(note);
        return noteMapper.toNoteResponse(updatedNote);
    }

    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserBookUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        noteRepository.delete(note);
    }

    private UserBook findUserBookAndVerifyOwner(Long userBookId, Long userId) {
        return userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserBook", "id", userBookId));
    }

    private void validatePageNumber(Integer pageNumber, UserBook userBook) {
        if (userBook.getBook() != null && userBook.getBook().getPageCount() != null && userBook.getBook().getPageCount() > 0) {
            if (pageNumber > userBook.getBook().getPageCount()) {
                throw new BadRequestException("Page number (" + pageNumber + ") cannot exceed total pages of book (" + userBook.getBook().getPageCount() + ")");
            }
        }
    }
}
