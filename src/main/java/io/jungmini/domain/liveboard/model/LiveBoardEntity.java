package io.jungmini.domain.liveboard.model;

import java.util.Objects;

import io.jungmini.domain.user.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "live_boards")
public class LiveBoardEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "live_board_id")
	private Long id;

	@Column(nullable = false, unique = true)
	private Long matchId;

	@Column(nullable = false)
	private String name;

	// JPA용
	protected LiveBoardEntity() {
	}

	@Builder
	public LiveBoardEntity(Long id, Long matchId, String name) {
		this.id = id;
		this.matchId = matchId;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		LiveBoardEntity that = (LiveBoardEntity)o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
