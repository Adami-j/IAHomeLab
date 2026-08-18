CREATE TABLE source_tag (
                            source_id UUID NOT NULL,
                            tag VARCHAR(100) NOT NULL,

                            CONSTRAINT fk_source_tag_source
                                FOREIGN KEY (source_id)
                                    REFERENCES source(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT pk_source_tag
                                PRIMARY KEY (source_id, tag)
);