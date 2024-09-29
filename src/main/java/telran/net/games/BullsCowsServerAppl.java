package telran.net.games;

import java.util.HashMap;

import telran.net.*;
import telran.net.games.config.BullsCowsPersistenceUnitInfo;
import static telran.net.games.config.BullsCowsConfigurationProperties.*;
import telran.net.games.controller.BullsCowsProtocol;
import telran.net.games.repo.*;
import telran.net.games.service.*;

public class BullsCowsServerAppl {

	public static void main(String[] args) {
		Protocol bullsCowsProtocol = getBullsCowsProtocol();
		TcpServer server = new TcpServer(bullsCowsProtocol, PORT);
		server.run();

	}

	private static Protocol getBullsCowsProtocol() {
		HashMap<String, Object> hibernateProperties = new HashMap<>();
		hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
		BullsCowsRepository repository = new BullsCowsRepositoryJpa(new BullsCowsPersistenceUnitInfo(), hibernateProperties);
		BullsCowsGameRunner bcRunner = new BullsCowsGameRunner(N_DIGITS);
		BullsCowsService bcService = new BullsCowsServiceImpl(repository, bcRunner);
		return new BullsCowsProtocol(bcService);
	}

}
