package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class GamerAlreadyExistsException extends IllegalStateException {
	public GamerAlreadyExistsException(String username) {
		super("Gamer " + username + "already exists");
	}
	
}
