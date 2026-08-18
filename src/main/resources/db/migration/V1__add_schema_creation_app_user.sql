CREATE TABLE app_user (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          username VARCHAR(100) NOT NULL,
                          email VARCHAR(255),
                          display_name VARCHAR(255),

                          role VARCHAR(30) NOT NULL DEFAULT 'USER',
                          enabled BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT uk_app_user_username UNIQUE (username),
                          CONSTRAINT uk_app_user_email UNIQUE (email),

                          CONSTRAINT chk_app_user_role
                              CHECK (role IN ('USER', 'ADMIN'))
);


CREATE TABLE user_identity (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               user_id UUID NOT NULL,

                               type VARCHAR(30) NOT NULL,
                               provider VARCHAR(50) NOT NULL,
                               provider_subject VARCHAR(255) NOT NULL,

                               password_hash VARCHAR(255),

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_user_identity_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES app_user(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uk_user_identity_provider_subject
                                   UNIQUE (provider, provider_subject),

                               CONSTRAINT chk_user_identity_type
                                   CHECK (
                                       type IN ('LOCAL', 'OIDC', 'OAUTH2')
                                       ),

                               CONSTRAINT chk_user_identity_password
                                   CHECK (
                                       (
                                           type = 'LOCAL'
                                               AND password_hash IS NOT NULL
                                           )
                                           OR
                                       (
                                           type <> 'LOCAL'
                                               AND password_hash IS NULL
                                           )
                                       )
);

CREATE INDEX idx_user_identity_user
    ON user_identity(user_id);