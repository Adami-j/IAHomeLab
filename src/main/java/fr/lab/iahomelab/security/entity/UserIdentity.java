package fr.lab.iahomelab.security.entity;


import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_identity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_identity_provider_subject",
                        columnNames = {"provider", "provider_subject"}
                )
        }
)
public class UserIdentity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdentityType identityType;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    protected UserIdentity() {
    }

}