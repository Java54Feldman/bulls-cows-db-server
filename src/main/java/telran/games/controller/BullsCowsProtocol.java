package telran.games.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import telran.net.*;
import telran.net.games.model.GameGamerDto;
import telran.net.games.model.MoveData;
import telran.net.games.model.SequenceGameGamerDto;
import telran.net.games.service.BullsCowsService;

public class BullsCowsProtocol implements Protocol {
	private BullsCowsService bcService;
	
	public BullsCowsProtocol(BullsCowsService bcService) {
		this.bcService = bcService;
	}

	@Override
	public Response getResponse(Request request) {
		String requestType = request.requestType();
		String requestData = request.requestData();
		Response response;
		try {
			response = switch (requestType) {
			case "createGame" -> createGame(requestData);
			case "startGame" -> startGame(requestData);
			case "registerGamer" -> registerGamer(requestData);
			case "gamerJoinGame" -> gamerJoinGame(requestData);
			case "getNotStartedGames" -> getNotStartedGames(requestData);
			case "moveProcessing" -> moveProcessing(requestData);
			case "gameOver" -> gameOver(requestData);
			case "getGameGamers" -> getGameGamers(requestData);
			default -> new Response(ResponseCode.WRONG_REQUEST_TYPE, 
					requestType);
			};
		} catch (Exception e) {
			response = new Response(ResponseCode.WRONG_REQUEST_DATA, 
					e.getMessage());
		}
		return response;
	}

	private Response createGame(String requestData) {
		long gameId = bcService.createGame();
		String responseString = Long.toString(gameId);
		return getResponseOk(responseString);
	}
	private Response startGame(String requestData) {
		long gameId = Long.parseLong(requestData);
		List<String> gamers = bcService.startGame(gameId);
		String responseString = resultsToJSON(gamers);
		return getResponseOk(responseString);
	}
	private Response registerGamer(String requestData) {
		//TODO просто геймер
		String responseString = null;
		return getResponseOk(responseString);
	}
	private Response gamerJoinGame(String requestData) {
		GameGamerDto gameGamer = new GameGamerDto(new JSONObject(requestData));
		bcService.gamerJoinGame(gameGamer.gameId(), gameGamer.username());
		String responseString = "";
		return getResponseOk(responseString);
	}
	private Response getNotStartedGames(String requestData) {
		//TODO
		String responseString = null;
		return getResponseOk(responseString);
	}
	private Response moveProcessing(String requestData) {
		SequenceGameGamerDto sggd = new SequenceGameGamerDto(new JSONObject(requestData));
		String moveSequence = sggd.sequence();
		long gameId = sggd.gameId();
		String username = sggd.username();
		List<MoveData> result = bcService.moveProcessing(moveSequence, gameId, username);
		String responseString = resultsToJSON(result);
		return getResponseOk(responseString);
	}
	private Response gameOver(String requestData) {
		//TODO
		String responseString = null;
		return getResponseOk(responseString);
	}
	private Response getGameGamers(String requestData) {
		//TODO
		String responseString = null;
		return getResponseOk(responseString);
	}

	private Response getResponseOk(String responseString) {
		return new Response(ResponseCode.OK, responseString);
	}
	private <T> String resultsToJSON(List<T> res) {

		return res.stream().map(T::toString)
				.collect(Collectors.joining(";"));
	}

}
