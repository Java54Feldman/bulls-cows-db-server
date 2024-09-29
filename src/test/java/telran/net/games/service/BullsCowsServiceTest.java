package telran.net.games.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import telran.net.games.BullsCowsTestPersistenceUnitInfo;
import telran.net.games.entities.Game;
import telran.net.games.entities.Gamer;
import telran.net.games.exceptions.*;
import telran.net.games.model.MoveData;
import telran.net.games.repo.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BullsCowsServiceTest {
	private static final int N_DIGITS = 4;
	static BullsCowsRepository repository;
	static BullsCowsService bcService;
	static {
		HashMap<String, Object> hibernateProperties = new HashMap<>();
		hibernateProperties.put("hibernate.hbm2ddl.auto", "create");
		repository = new BullsCowsRepositoryJpa(new BullsCowsTestPersistenceUnitInfo(), hibernateProperties);
		BullsCowsGameRunner bcRunner = new BullsCowsGameRunner(N_DIGITS);
		bcService = new BullsCowsServiceImpl(repository, bcRunner);
	}
	static long gameIdNormalFlow;
	static long gameIdAltFlow;
	static String gamerUsernameNormalFlow = "gamer1";
	static String gamerUsernameAlternativeFlow = "gamer99";

    @Test
    @Order(1)
    void createGameTest() {
    	gameIdNormalFlow = bcService.createGame();
    	gameIdAltFlow = bcService.createGame();
        Game game = repository.getGame(gameIdNormalFlow);
        assertNull(game.getDate());
        assertFalse(game.isFinished());
    }

    @Test
    @Order(2)
    void registerGamerTest() {
        bcService.registerGamer(gamerUsernameNormalFlow, LocalDate.of(2000, 1, 1));
        Gamer gamer = repository.getGamer(gamerUsernameNormalFlow);
        assertNotNull(gamer);
        assertThrowsExactly(GamerAlreadyExistsException.class,
                () -> bcService.registerGamer(gamerUsernameNormalFlow, LocalDate.of(2000, 1, 1)));
    }

    @Test
    @Order(3)
    void gamerJoinGameAndGetGameGamersTest() {
        bcService.gamerJoinGame(gameIdNormalFlow, gamerUsernameNormalFlow);
		runGamersTest(gameIdNormalFlow, gamerUsernameNormalFlow);

        bcService.registerGamer(gamerUsernameAlternativeFlow, LocalDate.of(1980, 11, 1));
        bcService.gamerJoinGame(gameIdAltFlow, gamerUsernameAlternativeFlow);
        runGamersTest(gameIdAltFlow, gamerUsernameAlternativeFlow);
        
        assertThrowsExactly(GameNotFoundException.class, () -> bcService.getGameGamers(1000L));
    }

	private void runGamersTest(long gameId, String username) {
		List<String> gamers = bcService.getGameGamers(gameId);
        assertEquals(1, gamers.size());
        assertEquals(username, gamers.get(0));
	}

    @Test
    @Order(4)
    void startGameNormalFlowTest() {
        assertFalse(repository.isGameStarted(gameIdNormalFlow));
        List<String> gamers = bcService.startGame(gameIdNormalFlow);
        assertEquals(1, gamers.size());
        assertEquals(gamerUsernameNormalFlow, gamers.get(0));
        assertTrue(repository.isGameStarted(gameIdNormalFlow));
        assertNotNull(repository.getGame(gameIdNormalFlow).getDate());
    }

    @Test
    @Order(5)
    void startGameAlternativeFlowTest() {
        long newGameId = bcService.createGame();
        assertThrowsExactly(NoGamerInGameException.class, () -> bcService.startGame(newGameId));
        assertThrowsExactly(GameNotFoundException.class, () -> bcService.startGame(1000L));
        assertEquals(2, bcService.getNotStartedGames().size()); // Two games left not started

        bcService.startGame(gameIdAltFlow);
        assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.startGame(gameIdAltFlow));
        assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.gamerJoinGame(gameIdAltFlow, gamerUsernameNormalFlow));
        assertEquals(1, bcService.getNotStartedGames().size()); // One game left not started
    }

    @Test
    @Order(6)
    void moveProcessingNormalFlowTest() {
        String toBeGuessed = ((BullsCowsServiceImpl) bcService).getSequence(gameIdNormalFlow);
        List<MoveData> moves = bcService.moveProcessing("9876", gameIdNormalFlow, gamerUsernameNormalFlow);
        assertEquals(1, moves.size());
        moves = bcService.moveProcessing("6543", gameIdNormalFlow, gamerUsernameNormalFlow);
        assertEquals(2, moves.size()); 
        moves = bcService.moveProcessing(toBeGuessed, gameIdNormalFlow, gamerUsernameNormalFlow);
        assertEquals(3, moves.size()); 
        assertEquals(new MoveData(toBeGuessed, 4, 0), moves.get(2));
        assertTrue(repository.isGameFinished(gameIdNormalFlow));
        assertTrue(repository.isWinner(gameIdNormalFlow, gamerUsernameNormalFlow));
    }

    @Test
    @Order(7)
    void moveProcessingAlternativeFlowTest() {
        String toBeGuessed = ((BullsCowsServiceImpl) bcService).getSequence(gameIdAltFlow);
        assertThrowsExactly(IncorrectMoveSequenceException.class, () -> bcService.moveProcessing("1111", gameIdAltFlow, gamerUsernameAlternativeFlow));
        List<MoveData> moves = bcService.moveProcessing(toBeGuessed, gameIdAltFlow, gamerUsernameAlternativeFlow);
        assertEquals(1, moves.size());
        assertEquals(new MoveData(toBeGuessed, 4, 0), moves.get(0));
        assertTrue(repository.isGameFinished(gameIdAltFlow));
        assertTrue(repository.isWinner(gameIdAltFlow, gamerUsernameAlternativeFlow));
        assertThrowsExactly(GameFinishedException.class, () -> bcService.moveProcessing("1234", gameIdAltFlow, gamerUsernameAlternativeFlow));
    }

    @Test
    @Order(8)
    void gameOverTest() {
        assertTrue(bcService.gameOver(gameIdNormalFlow));
        assertTrue(bcService.gameOver(gameIdAltFlow));
        assertFalse(bcService.gameOver(bcService.createGame()));
    }
    
    @Test
    @Order(9)
    void getNotStartedGamesWithGamerTest() {
        long newGameId = bcService.createGame();
        bcService.gamerJoinGame(newGameId, gamerUsernameNormalFlow);

        List<Long> games = bcService.getNotStartedGamesWithGamer(gamerUsernameNormalFlow);
        assertTrue(games.contains(newGameId));
        assertFalse(games.contains(gameIdNormalFlow)); 
        assertFalse(games.contains(gameIdAltFlow)); 

        assertThrowsExactly(GamerNotFoundException.class, 
            () -> bcService.getNotStartedGamesWithGamer("nonexistentGamer"));
    }

    @Test
    @Order(10)
    void getNotStartedGamesWithNoGamerTest() {
        long newGameId = bcService.createGame();

        List<Long> games = bcService.getNotStartedGamesWithNoGamer(gamerUsernameNormalFlow);
        assertTrue(games.contains(newGameId));
        assertFalse(games.contains(gameIdNormalFlow)); 
        assertFalse(games.contains(gameIdAltFlow)); 

        assertThrowsExactly(GamerNotFoundException.class, 
            () -> bcService.getNotStartedGamesWithNoGamer("nonexistentGamer"));
    }

    @Test
    @Order(11)
    void getStartedGamesWithGamerTest() {
        List<Long> games = bcService.getStartedGamesWithGamer(gamerUsernameNormalFlow);
        assertTrue(games.contains(gameIdNormalFlow));
        assertFalse(games.contains(gameIdAltFlow)); 

        assertThrowsExactly(GamerNotFoundException.class, 
            () -> bcService.getStartedGamesWithGamer("nonexistentGamer"));
    }
}
