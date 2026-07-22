package com.tsundoku.backend.notes.repository;

import com.tsundoku.backend.notes.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByUserBookId(Long userBookId, Pageable pageable);

    Page<Note> findByUserBookIdAndPageNumber(Long userBookId, Integer pageNumber, Pageable pageable);

    Optional<Note> findByIdAndUserBookUserId(Long noteId, Long userId);
}
