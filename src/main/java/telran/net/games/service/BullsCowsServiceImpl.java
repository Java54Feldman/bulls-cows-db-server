package telran.net.games.service;

import java.time.*;
import java.util.List;

import telran.net.games.entities.*;
import telran.net.games.exceptions.*;
import telran.net.games.model.*;
import telran.net.games.repo.BullsCowsRepository;

public class BullsCowsServiceImpl implements BullsCowsService {
	private BullsCowsRepository bcRepository;
	private BullsCowsGameRunner bcRunner;
	
	
	public BullsCowsServiceImpl(BullsCowsRepository bcRepository, BullsCowsGameRunner bcRunner) {
		this.bcRepository = bcRepository;
		this.bcRunner = bcRunner;
	}
	/**
	 * Login gamer
	 * returns username
	 */
	@Override 
	public String loginGamer(String username) {
		Gamer gamer = bcRepository.getGamer(username);
		return gamer.getUsername();
	}
	@Override
	/**
	 * Creates new game
	 * returns ID of the created game
	 */
	public long createGame() {
		return bcRepository.createNewGame(bcRunner.getRandomSequence());
	}
	@Override
	/**
	 * starts game
	 * returns list of gamers (user names)
	 * exceptions:
	 * GameNotFoundException
	 * GameAlreadyStartedException
	 * NoGamerInGameException
	 */
	public List<String> startGame(long gameId) {
		checkIsGameAlreadyStarted(gameId);
		List<String> result = bcRepository.getGameGamers(gameId);
		if(result.isEmpty() ) {
			throw new NoGamerInGameException(gameId);
		}
		bcRepository.setStartDate(gameId, LocalDateTime.now());
		return result;
	}
	@Override
	/**
	 * adds new gamer
	 * Exceptions:
	 * GamerAlreadyExistsException
	 */
	public void registerGamer(String username, LocalDate birthdate) {
		bcRepository.createNewGamer(username, birthdate);
		
	}
	@Override
	/**
	 * join a given gamer to a given game
	 * Exceptions:
	 * GameNotFoundException
	 * GameAlreadyStartedException
	 * GamerNotFoundException
	 * GameGamerAlreadyExistsException
	 */
	public void gamerJoinGame(long gameId, String username) {
		checkIsGameAlreadyStarted(gameId);
		bcRepository.createGameGamer(gameId, username);
		
	}
	private void checkIsGameAlreadyStarted(long gameId) {
		if(bcRepository.isGameStarted(gameId)) {
			throw new GameAlreadyStartedException(gameId);
		}
	}
	@Override
	/**
	 * returns list of ID's for not started games
	 * no exceptions (empty list is allowed)
	 */
	public List<Long> getNotStartedGames() {
		return bcRepository.getGameIdsNotStarted();
	}
	@Override
	/**
	 * returns all objects of MoveData of a given game and given gamer
	 * including the last with the given parameters
	 * in the case of the winner's move the game should be set as finished
	 * and the gamer in the game should be set as the winner
	 * Exceptions:
	 * IncorrectMoveSequenceException (extends IllegalArgumentException)
	 * GameNotFoundException
	 * GamerNotFoundException
	 * GameNotStartedException (extends IllegalStateException)
	 * GameFinishedException (extends IllegalStateException)
	 * GameGamerNotFoundExeption
	 */
	public List<MoveData> moveProcessing(String moveSequence, long gameId, String username) {
		if(!bcRunner.checkGuess(moveSequence)) {
			throw new IncorrectMoveSequenceException(moveSequence, bcRunner.nDigits);
		}
		bcRepository.getGamer(username); //only for checking whether the gamer exists
		if(!bcRepository.isGameStarted(gameId)) {
			throw new GameNotStartedException(gameId);
		}
		if(bcRepository.isGameFinished(gameId)) {
			throw new GameFinishedException(gameId);
		}
		String toBeGuessedSequence = getSequence(gameId);
		MoveData moveData = bcRunner.moveProcessing(moveSequence, toBeGuessedSequence);
		MoveDto moveDto = new MoveDto(gameId, username, moveSequence,
				moveData.bulls(), moveData.cows());
		bcRepository.createGameGamerMove(moveDto);
		List<MoveData> result = bcRepository.getAllGameGamerMoves(gameId, username);
		if(bcRunner.checkGameFinished(moveData)) {
			finishGame(gameId, username);
		}
		return result;
	}
	private void finishGame(long gameId, String username) {
		bcRepository.setIsFinished(gameId);
		bcRepository.setWinner(gameId, username);
		
	}
	@Override
	/**
	 * returns true if game is finished
	 * Exceptions:
	 * GameNotFoundException
	 */
	public boolean gameOver(long gameId) {
		return bcRepository.isGameFinished(gameId);
	}
	@Override
	/**
	 * returns list of gamers in a given game
	 * Exceptions: 
	 * GameNotFoundException
	 */
	public List<String> getGameGamers(long gameId) {
		bcRepository.getGame(gameId);
		return bcRepository.getGameGamers(gameId);
	}
	/**
	 * Implied that the test class resides at the same package (to access the method)
	 * 
	 * @param gameId
	 * @return To be guessed sequence
	 * No Exceptions, that is implied that at the test gameId exists
	 */
	String getSequence(long gameId) {
		Game game = bcRepository.getGame(gameId);
		return game.getSequence();
	}
	@Override
	public List<Long> getNotStartedGamesWithGamer(String username) {
		bcRepository.getGamer(username);
		return bcRepository.getNotStartedGamesWithGamer(username);
	}
	@Override
	public List<Long> getNotStartedGamesWithNoGamer(String username) {
		bcRepository.getGamer(username);
		return bcRepository.getNotStartedGamesWithNoGamer(username);
	}
	@Override
	public List<Long> getStartedGamesWithGamer(String username) {
		bcRepository.getGamer(username);
		return bcRepository.getStartedGamesWithGamer(username);
	}
	
}
