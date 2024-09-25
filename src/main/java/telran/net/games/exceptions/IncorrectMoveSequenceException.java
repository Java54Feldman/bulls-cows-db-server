package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class IncorrectMoveSequenceException extends IllegalArgumentException {
	public IncorrectMoveSequenceException(String sequence) {
		super(String.format("Sequence %s is incorrect", sequence));
	}

}
