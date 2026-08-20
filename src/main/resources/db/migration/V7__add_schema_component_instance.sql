CREATE TABLE component_instance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    setup_version_id UUID NOT NULL,

    name VARCHAR(200) NOT NULL,
    type VARCHAR(40) NOT NULL,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_component_instance_setup_version
        FOREIGN KEY (setup_version_id)
            REFERENCES setup_version(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_component_instance_version_name
        UNIQUE (setup_version_id, name),

    CONSTRAINT chk_component_instance_type
        CHECK (type IN (
            'LLM',
            'PROMPT',
            'RETRIEVER',
            'EMBEDDING_MODEL',
            'VECTOR_STORE',
            'RERANKER',
            'TOOL',
            'OCR',
            'STT',
            'TTS',
            'VISION',
            'OTHER'
        ))
);
