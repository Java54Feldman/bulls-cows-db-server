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
	static long gameIdNormalFlow = bcService.createGame();
	static long gameIdAlternativeFlow = bcService.createGame();
	static String gamerUsernameNormalFlow = "gamer1";
	static String gamerUsernameAlternativeFlow = "gamer99";

    @Test
    @Order(1)
    void createGameTest() {
        Game game = repository.getGame(gameIdNormalFlow);
        assertNotNull(game);
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
        List<String> gamers = bcService.getGameGamers(gameIdNormalFlow);
        assertEquals(1, gamers.size());
        assertEquals(gamerUsernameNormalFlow, gamers.get(0));

        bcService.registerGamer(gamerUsernameAlternativeFlow, LocalDate.of(1980, 11, 1));
        bcService.gamerJoinGame(gameIdAlternativeFlow, gamerUsernameAlternativeFlow);
        gamers = bcService.getGameGamers(gameIdAlternativeFlow);
        assertEquals(1, gamers.size());
        assertEquals(gamerUsernameAlternativeFlow, gamers.get(0));

        assertThrowsExactly(GameNotFoundException.class, () -> bcService.getGameGamers(1000L));
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

        bcService.startGame(gameIdAlternativeFlow);
        assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.startGame(gameIdAlternativeFlow));
        assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.gamerJoinGame(gameIdAlternativeFlow, gamerUsernameNormalFlow));
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
        String toBeGuessed = ((BullsCowsServiceImpl) bcService).getSequence(gameIdAlternativeFlow);
        assertThrowsExactly(IncorrectMoveSequenceException.class, () -> bcService.moveProcessing("1111", gameIdAlternativeFlow, gamerUsernameAlternativeFlow));
        List<MoveData> moves = bcService.moveProcessing(toBeGuessed, gameIdAlternativeFlow, gamerUsernameAlternativeFlow);
        assertEquals(1, moves.size());
        assertEquals(new MoveData(toBeGuessed, 4, 0), moves.get(0));
        assertTrue(repository.isGameFinished(gameIdAlternativeFlow));
        assertTrue(repository.isWinner(gameIdAlternativeFlow, gamerUsernameAlternativeFlow));
        assertThrowsExactly(GameFinishedException.class, () -> bcService.moveProcessing("1234", gameIdAlternativeFlow, gamerUsernameAlternativeFlow));
    }

    @Test
    @Order(8)
    void gameOverTest() {
        assertTrue(bcService.gameOver(gameIdNormalFlow));
        assertTrue(bcService.gameOver(gameIdAlternativeFlow));
        assertFalse(bcService.gameOver(bcService.createGame()));
    }

}
