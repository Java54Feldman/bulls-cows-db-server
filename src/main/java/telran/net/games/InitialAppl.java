package telran.net.games;

import java.util.HashMap;

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
		Gamer gamer = em.find(Gamer.class, "gamer1");
		System.out.println(gamer);
	}

}
