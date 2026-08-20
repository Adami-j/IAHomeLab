package fr.lab.iahomelab.setup.entity;

import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "setup_version",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_setup_version_setup_number",
                columnNames = {"setup_id", "version_number"}
        )
)
public class SetupVersion extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setup_id", nullable = false)
    private Setup setup;

    @NotNull
    @Min(1)
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SetupVersionStatus status = SetupVersionStatus.DRAFT;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
