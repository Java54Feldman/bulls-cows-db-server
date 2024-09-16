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
}
