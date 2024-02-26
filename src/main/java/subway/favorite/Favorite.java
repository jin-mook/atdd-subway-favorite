package subway.favorite;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import subway.member.Member;
import subway.station.Station;

@Entity
public class Favorite {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "source_station_id", nullable = false)
	private Station sourceStation;

	@ManyToOne
	@JoinColumn(name = "target_station_id", nullable = false)
	private Station targetStation;

	protected Favorite() {
	}

	public Favorite(Member member, Station sourceStation, Station targetStation) {
		this.member = member;
		this.sourceStation = sourceStation;
		this.targetStation = targetStation;
	}

	public Favorite(Station sourceStation, Station targetStation) {
		this.sourceStation = sourceStation;
		this.targetStation = targetStation;
	}

	public Long getId() {
		return id;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Station getSourceStation() {
		return sourceStation;
	}

	public Station getTargetStation() {
		return targetStation;
	}
}
