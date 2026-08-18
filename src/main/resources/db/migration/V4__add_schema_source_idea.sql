CREATE TABLE source_idea (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             source_id UUID NOT NULL,

                             title VARCHAR(300) NOT NULL,
                             content TEXT NOT NULL,
                             type VARCHAR(20) NOT NULL,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_source_idea_source
                                 FOREIGN KEY (source_id)
                                     REFERENCES source(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_source_idea_type
                                 CHECK (type IN ('IDEA', 'CLAIM'))
);