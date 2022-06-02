package com.esempla.file.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "document")
public class Document implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @JsonIgnore
    @Size(min = 1, max = 255)
    @Column(name = "uuid_doc")
    private String uuidDoc;

    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "title")
    private String title;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "path")
    private String path;

    @NotNull
    @Column(name = "size")
    private Long size;

    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "mime_type")
    private String mimeType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUuidDoc() {
        return uuidDoc;
    }

    public void setUuidDoc(String uuidDoc) {
        this.uuidDoc = uuidDoc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && Objects.equals(title, document.title) && Objects.equals(path, document.path) && Objects.equals(size, document.size) && Objects.equals(mimeType, document.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, path, size, mimeType);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
