package io.jungmini.domain.match;

import io.jungmini.domain.team.TeamEntity;
import io.jungmini.domain.user.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches")
public class MatchEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "match_id")
	private Long matchId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "home_team_id", nullable = false)
	private TeamEntity homeTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "away_team_id", nullable = false)
	private TeamEntity awayTeam;

	// 경기장은 일단 생략

	@Column
	@Enumerated(value = EnumType.STRING)
	private MatchResult matchResult;

	@Column
	private Integer homeScore;

	@Column
	private Integer awayScore;

	@Column
	private String remark; // 비고

	// for JPA
	protected MatchEntity() {
	}

	public Long getMatchId() {
		return matchId;
	}

	public TeamEntity getHomeTeam() {
		return homeTeam;
	}

	public TeamEntity getAwayTeam() {
		return awayTeam;
	}

	public MatchResult getMatchResult() {
		return matchResult;
	}

	public Integer getHomeScore() {
		return homeScore;
	}

	public Integer getAwayScore() {
		return awayScore;
	}

	public String getRemark() {
		return remark;
	}

	@Override
	public String toString() {
		return "MatchEntity{matchId=%d, homeTeam=%s, awayTeam=%s, matchResult=%s, homeScore=%d, awayScore=%d, remark='%s'}"
			.formatted(matchId, homeTeam, awayTeam, matchResult, homeScore, awayScore, remark);
	}
}
