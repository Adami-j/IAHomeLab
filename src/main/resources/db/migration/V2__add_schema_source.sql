CREATE TABLE source (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        title VARCHAR(500) NOT NULL,

                        url VARCHAR(2048),
                        storage_path VARCHAR(2048),
                        file_name VARCHAR(500),
                        mime_type VARCHAR(255),

                        type VARCHAR(30) NOT NULL,
                        status VARCHAR(30) NOT NULL DEFAULT 'TO_READ',

                        summary TEXT,
                        notes TEXT,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT uk_source_url UNIQUE (url),

                        CONSTRAINT chk_source_location
                            CHECK (
                                url IS NOT NULL
                                    OR storage_path IS NOT NULL
                                ),

                        CONSTRAINT chk_source_type
                            CHECK (type IN (
                                            'ARTICLE',
                                            'PAPER',
                                            'REPOSITORY',
                                            'DOCUMENTATION',
                                            'VIDEO',
                                            'OTHER'
                                )),

                        CONSTRAINT chk_source_status
                            CHECK (status IN (
                                              'TO_READ',
                                              'READ',
                                              'INTERESTING',
                                              'ARCHIVED'
                                ))
);