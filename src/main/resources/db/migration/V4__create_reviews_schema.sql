-- V4: Reviews and Rating Schema

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_book_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(200),
    content TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    recommend BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user_book FOREIGN KEY (user_book_id) REFERENCES user_books (id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_user_book UNIQUE (user_book_id)
);

CREATE INDEX idx_reviews_user_book_id ON reviews(user_book_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
