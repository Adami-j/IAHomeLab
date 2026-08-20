CREATE TABLE setup_connection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    setup_version_id UUID NOT NULL,
    source_component_id UUID NOT NULL,
    target_component_id UUID NOT NULL,

    name VARCHAR(200),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_setup_connection_setup_version
        FOREIGN KEY (setup_version_id)
            REFERENCES setup_version(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_setup_connection_source_component
        FOREIGN KEY (source_component_id)
            REFERENCES component_instance(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_setup_connection_target_component
        FOREIGN KEY (target_component_id)
            REFERENCES component_instance(id)
            ON DELETE CASCADE
);
