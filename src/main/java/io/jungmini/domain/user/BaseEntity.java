package io.jungmini.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class BaseEntity {

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public LocalDateTime getCreateAt() {
		return createAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	@PrePersist
	public void prePersist() {
		this.createAt = LocalDateTime.now();
		this.updatedAt = this.createAt;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}