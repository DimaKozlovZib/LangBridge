package LangBridge.UserService.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity mapping to the existing "users" table created by Flyway.
 * Matches column names and types; validation mirrors DB constraints where practical.
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_users_email", columnNames = {"email"})
       })
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 10)
    @Column(name = "native_lang", nullable = false, length = 10)
    private String nativeLang;

    @NotBlank
    @Size(max = 10)
    @Column(name = "target_lang", nullable = false, length = 10)
    private String targetLang;

    @Column(name = "level", nullable = false)
    private short level = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public User() {
        // JPA requires a no-arg constructor
    }

    // Cross-field validation moved to DTO layer

    public UUID getId() {
        return id;
    }

    // no public setter for id per design

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNativeLang() {
        return nativeLang;
    }

    public void setNativeLang(String nativeLang) {
        this.nativeLang = nativeLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public short getLevel() { return level; }
    public void setLevel(short level) { this.level = level; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // no public setter for createdAt per design

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nativeLang='" + nativeLang + '\'' +
                ", targetLang='" + targetLang + '\'' +
                ", level=" + level +
                ", createdAt=" + createdAt +
                '}';
    }
}
