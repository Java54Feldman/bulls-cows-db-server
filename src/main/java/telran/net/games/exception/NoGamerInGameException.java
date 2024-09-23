package telran.net.games.exception;

@SuppressWarnings("serial")
public class NoGamerInGameException extends IllegalStateException {
	public NoGamerInGameException(long gameId) {
		super("No gamers in game " + gameId);
	}
}
