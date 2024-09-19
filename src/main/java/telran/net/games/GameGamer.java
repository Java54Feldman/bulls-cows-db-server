package telran.net.games;
import jakarta.persistence.*;
@Entity
@Table(name = "game_gamer")
public class GameGamer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	@JoinColumn(name = "game_id")
	private Game game;
	@ManyToOne
	@JoinColumn(name = "gamer_id")
	private Gamer gamer;
	@Column(name="is_winner", nullable = false)
	private boolean isWinner;
	
	public GameGamer() {
		
	}
	public GameGamer(boolean isWinner, Game game, Gamer gamer) {
		this.isWinner = isWinner;
		this.game = game;
		this.gamer = gamer;
	}
	public long getId() {
		return id;
	}
	public Game getGame() {
		return game;
	}
	public Gamer getGamer() {
		return gamer;
	}
	public boolean isWinner() {
		return isWinner;
	}
			
}
