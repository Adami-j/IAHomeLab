package fr.lab.iahomelab.setup.entity;

import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "setup_connection")
public class Connection extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setup_version_id", nullable = false)
    private SetupVersion setupVersion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_component_id", nullable = false)
    private ComponentInstance sourceComponent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_component_id", nullable = false)
    private ComponentInstance targetComponent;

    @Size(max = 200)
    @Column(name = "name", length = 200)
    private String name;
}
