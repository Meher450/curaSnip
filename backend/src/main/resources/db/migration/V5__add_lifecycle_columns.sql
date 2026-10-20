-- V5__add_lifecycle_columns.sql
ALTER TABLE pastes ADD COLUMN expires_at TIMESTAMPTZ;
ALTER TABLE pastes ADD COLUMN burn_after_read BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE pastes ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE pastes ADD COLUMN delete_token_hash VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE pastes ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_pastes_expires_at ON pastes(expires_at) WHERE expires_at IS NOT NULL;