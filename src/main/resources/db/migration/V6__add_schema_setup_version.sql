CREATE TABLE setup_version (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               setup_id UUID NOT NULL,
                               version_number INTEGER NOT NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                               description TEXT,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_setup_version_setup
                                   FOREIGN KEY (setup_id)
                                       REFERENCES setup(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uk_setup_version_setup_number
                                   UNIQUE (setup_id, version_number),

                               CONSTRAINT chk_setup_version_number
                                   CHECK (version_number > 0),

                               CONSTRAINT chk_setup_version_status
                                   CHECK (status IN ('DRAFT', 'FROZEN'))
);

CREATE INDEX idx_setup_version_setup_id
    ON setup_version(setup_id);
