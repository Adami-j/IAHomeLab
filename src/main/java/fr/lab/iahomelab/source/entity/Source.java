package fr.lab.iahomelab.source.entity;

import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "source", uniqueConstraints = {@UniqueConstraint(name = "uk_source_url",
        columnNames = {"url"})})
public class Source extends BaseEntity {

    @Size(max = 500)
    @NotNull
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Size(max = 2048)
    @Column(name = "url", length = 2048)
    private String url;

    @Size(max = 2048)
    @Column(name = "storage_path", length = 2048)
    private String storagePath;

    @Size(max = 500)
    @Column(name = "file_name", length = 500)
    private String fileName;

    @Size(max = 255)
    @Column(name = "mime_type")
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private SourceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SourceStatus status = SourceStatus.TO_READ;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

}