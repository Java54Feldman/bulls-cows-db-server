package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class GameNotStartedException extends IllegalStateException {
	public GameNotStartedException(long gameId) {
		super(String.format("Game %d not started", gameId));
	}

}
