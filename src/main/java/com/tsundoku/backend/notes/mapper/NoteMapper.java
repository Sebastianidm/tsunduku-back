package com.tsundoku.backend.notes.mapper;

import com.tsundoku.backend.notes.dto.NoteResponse;
import com.tsundoku.backend.notes.entity.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponse toNoteResponse(Note note) {
        if (note == null) {
            return null;
        }

        return NoteResponse.builder()
                .id(note.getId())
                .userBookId(note.getUserBook() != null ? note.getUserBook().getId() : null)
                .pageNumber(note.getPageNumber())
                .content(note.getContent())
                .quote(note.getQuote())
                .isSpoiler(note.getIsSpoiler())
                .tags(note.getTags())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
