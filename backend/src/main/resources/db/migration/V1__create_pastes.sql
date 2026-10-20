CREATE TABLE pastes (
                        id BIGSERIAL PRIMARY KEY,

                        content TEXT,

                        short_id VARCHAR(12) NOT NULL UNIQUE,

                        created_at TIMESTAMPTZ,

                        content_location VARCHAR(512),

                        size_bytes INTEGER NOT NULL
);