package fr.lab.iahomelab.security.entity;


import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
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
    @Column(name = "type", nullable = false, length = 30)
    private IdentityType identityType;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    protected UserIdentity() {
    }

    public UserIdentity(
            AppUser user,
            IdentityType identityType,
            String provider,
            String providerSubject,
            String passwordHash
    ) {
        this.user = user;
        this.identityType = identityType;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.passwordHash = passwordHash;
    }

}