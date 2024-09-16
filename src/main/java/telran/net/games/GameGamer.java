package telran.net.games;
import jakarta.persistence.*;
@Entity
@Table(name = "game_gamer")
public class GameGamer {
	@Id
	private long id;
	@ManyToOne
	@JoinColumn(name = "game_id")
	private Game game;
	@ManyToOne
	@JoinColumn(name = "gamer_id")
	private Gamer gamer;
	private boolean is_winner;
	public long getId() {
		return id;
	}
	public Game getGame() {
		return game;
	}
	public Gamer getGamer() {
		return gamer;
	}
	public boolean isIs_winner() {
		return is_winner;
	}
	@Override
	public String toString() {
		return "GameGamer [id=" + id + 
				", game=" + game.getId() + 
				", gamer=" + gamer.getUsername() + 
				", is_winner=" + is_winner + "]";
	}
			

}
