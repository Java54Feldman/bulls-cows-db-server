package telran.net.games;
import java.time.*;
import java.util.List;
import jakarta.persistence.*;

public class JpqlQueriesRepository {
	private EntityManager em;

	public JpqlQueriesRepository(EntityManager em) {
		this.em = em;
	}
	public List<Game> getGamesFinished(boolean isFinished) {
		TypedQuery<Game> query = em.createQuery(
				"select game from Game game where is_finished=?1",
				Game.class);
		List<Game> res = query.setParameter(1, isFinished).getResultList();
		return res;
	}
	public List<DateTimeSequence> getDateTimeSequence(LocalTime time) {
		TypedQuery<DateTimeSequence> query = 
				em.createQuery("select date, sequence "
						+ "from Game where cast(date as time) < :time",
						DateTimeSequence.class);
		List<DateTimeSequence> res = query.setParameter("time", time).getResultList();
		return res;
	}
	public List<Integer> getBullsInMovesGamersBornAfter(LocalDate afterDate) {
		TypedQuery<Integer> query = em.createQuery(
				"select bulls from Move where gameGamer.gamer.birthdate > ?1", 
				Integer.class);
		List<Integer> res = query.setParameter(1, afterDate).getResultList();
		return res;
	}
	public List<MinMaxAmount> getDistributionGamesMoves(int interval) {
//		select 
//	    floor (game_moves / 5) * 5 as min_moves,
//	    floor (game_moves / 5) * 5 + 4 as max_moves,
//	    count(*) as amount
//	    from 
//	    	(select count(*) as game_moves from 
//			game_gamer join move on game_gamer_id = game_gamer.id
//			group by game_id) as game_moves_table
//	    group by min_moves 
//	    order by min_moves;
		TypedQuery<MinMaxAmount> query = em.createQuery(
				"select floor(game_moves / :interval) * :interval as min_moves,"
				+ "floor(game_moves / :interval) * :interval + (:interval - 1) as max_moves,"
				+ "count(*) as amount "
				+ "from "
				+ "(select count(*) as game_moves from Move "
				+ "group by gameGamer.game.id) "
				+ "group by min_moves, max_moves "
				+ "order by min_moves", 
				MinMaxAmount.class);
		List<MinMaxAmount> res = query.setParameter("interval", interval).getResultList();
		return res;
	}
	
	public List<Game> getGamesAvgAgeGreater(int age) {
//		select * from game where id in 
//		(select game_id from game_gamer join gamer on gamer_id = username
//		group by game_id having avg(extract (year from age(birthdate))) > 60)
		int bornYear = LocalDate.now().minusYears(age).getYear();
		TypedQuery<Game> query = em.createQuery(
				"select gameRes from Game gameRes where id in "
				+ "(select game.id from GameGamer group by game.id "
				+ "having avg(extract(year from gamer.birthdate)) < ?1)",
				Game.class);
		List<Game> res = query.setParameter(1, bornYear).getResultList();
		return res;
	}
	public List<GameNumberMove> getGamesWinnerMovesLess(int numberMoves) {
//		select game_id, count(*)as moves from game_gamer
//		join move on game_gamer.id=game_gamer_id where is_winner
//		group by game_id having count(*) < 5
		TypedQuery<GameNumberMove> query = em.createQuery(
				"select gameGamer.game.id as gameId, count(*) from "
				+ "Move where gameGamer.is_winner = true "
				+ "group by gameId having count(*) < ?1",
				GameNumberMove.class);
		List<GameNumberMove> res = query.setParameter(1, numberMoves).getResultList();
		return res;
	}
	public List<String> getGamersWithMovesLess(int numberMoves) {
//		select distinct gamer_id from game_gamer
//		join move on game_gamer.id = game_gamer_id
//		group by game_id, gamer_id having count(*) < 4
		TypedQuery<String> query = em.createQuery(
				"select distinct gameGamer.gamer.username from Move "
				+ "group by gameGamer.game.id, gameGamer.gamer.username "
				+ "having count(*) < :numberMoves",
				String.class);
		List<String> res = query.setParameter("numberMoves", numberMoves).getResultList();
		return res;				
	}
	public List<GameAvgNumberMove> getGamesWithAvgMoves() {
//		select game_id, round(avg(moves), 1) from 
//		(select game_id, gamer_id, count(*) moves
//			from game_gamer	join move on game_gamer.id=game_gamer_id
//			group by game_id, gamer_id order by game_id)
//		group by game_id
		TypedQuery<GameAvgNumberMove> query = em.createQuery(
				"select gameId, round(avg(moves), 1) from "
				+ "(select gameGamer.game.id as gameId, gameGamer.gamer.username as gamerId, count(*) as moves "
				+ "from Move group by gameId, gamerId order by gameId) "
				+ "group by gameId",
				GameAvgNumberMove.class
				);
		List<GameAvgNumberMove> res = query.getResultList();
		return res;
	}
	
}
