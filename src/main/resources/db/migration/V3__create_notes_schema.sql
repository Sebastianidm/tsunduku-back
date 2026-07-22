-- V3: Notes and Quotes Schema

CREATE TABLE IF NOT EXISTS notes (
    id BIGSERIAL PRIMARY KEY,
    user_book_id BIGINT NOT NULL,
    page_number INT NOT NULL,
    content TEXT NOT NULL,
    quote TEXT,
    is_spoiler BOOLEAN NOT NULL DEFAULT FALSE,
    tags VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_user_book FOREIGN KEY (user_book_id) REFERENCES user_books (id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_user_book_id ON notes(user_book_id);
CREATE INDEX idx_notes_page_number ON notes(user_book_id, page_number);
