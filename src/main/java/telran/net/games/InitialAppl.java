package telran.net.games;

import java.util.*;
import org.hibernate.jpa.HibernatePersistenceProvider;
import jakarta.persistence.*;

public class InitialAppl {

	public static void main(String[] args) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("hibernate.hbm2ddl.auto", "update"); //using existing table
		map.put("hibernate.show_sql", true);
		map.put("hibernate.format_sql", true);
		
		EntityManagerFactory emFactory = new HibernatePersistenceProvider()
				.createContainerEntityManagerFactory(new BullsCowsPersistenceUnitInfo(), map);
		EntityManager em = emFactory.createEntityManager();
		
		JpqlQueriesRepository repository = new JpqlQueriesRepository(em);
		
		List<Game> games = repository.getGamesAvgAgeGreater(60);
		displayResult(games);
		
		List<GameNumberMove> gamesMoves = repository.getGamesWinnerMovesLess(5);
		displayResult(gamesMoves);
		
		List<String> gamers = repository.getGamersWithMovesLess(4);
		displayResult(gamers);
		
		List<GameAvgNumberMove> gamesAvgMoves = repository.getGamesWithAvgMoves();
		displayResult(gamesAvgMoves);
	}

	private static <T> void displayResult(List<T> list) {
		list.forEach(System.out::println);
		System.out.println(list.size());
	}

}
