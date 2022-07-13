package it.polito.tdp.PremierLeague.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.polito.tdp.PremierLeague.model.Action;
import it.polito.tdp.PremierLeague.model.Coppia;
import it.polito.tdp.PremierLeague.model.Match;
import it.polito.tdp.PremierLeague.model.Player;
import it.polito.tdp.PremierLeague.model.Team;

public class PremierLeagueDAO {
	
	public List<Player> listAllPlayers(){
		String sql = "SELECT * FROM Players";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				result.add(player);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Team> listAllTeams(){
		String sql = "SELECT * FROM Teams";
		List<Team> result = new ArrayList<Team>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Team team = new Team(res.getInt("TeamID"), res.getString("Name"));
				result.add(team);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> listAllActions(){
		String sql = "SELECT * FROM Actions";
		List<Action> result = new ArrayList<Action>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {
				
				Action action = new Action(res.getInt("PlayerID"),res.getInt("MatchID"),res.getInt("TeamID"),res.getInt("Starts"),res.getInt("Goals"),
						res.getInt("TimePlayed"),res.getInt("RedCards"),res.getInt("YellowCards"),res.getInt("TotalSuccessfulPassesAll"),res.getInt("totalUnsuccessfulPassesAll"),
						res.getInt("Assists"),res.getInt("TotalFoulsConceded"),res.getInt("Offsides"));
				
				result.add(action);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Match> listAllMatches(){
		String sql = "SELECT m.MatchID, m.TeamHomeID, m.TeamAwayID, m.teamHomeFormation, m.teamAwayFormation, m.resultOfTeamHome, m.date, t1.Name, t2.Name   "
				+ "FROM Matches m, Teams t1, Teams t2 "
				+ "WHERE m.TeamHomeID = t1.TeamID AND m.TeamAwayID = t2.TeamID "
				+ "ORDER BY m.MatchID";
		List<Match> result = new ArrayList<Match>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				
				Match match = new Match(res.getInt("m.MatchID"), res.getInt("m.TeamHomeID"), res.getInt("m.TeamAwayID"), res.getInt("m.teamHomeFormation"), 
							res.getInt("m.teamAwayFormation"),res.getInt("m.resultOfTeamHome"), res.getTimestamp("m.date").toLocalDateTime(), res.getString("t1.Name"),res.getString("t2.Name"));
				
				
				result.add(match);

			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	/*
	 *  Alla pressione del bottone “Crea Grafo”, si crei un grafo 
		semplice, pesato e orientato in cui nodi siano tutti i
		giocatori che hanno preso parte alla partita m. A tal 
		proposito, si considerino i giocatori che compaiono nella 
		tabella Actions: ogni riga di questa tabella riassume le 
		statistiche di gioco di un particolare giocatore per una 
		determinata partita.
	 * 
	 */
	public List<Player> getAllVertici(Match m){
		String sql = "SELECT p.PlayerID, p.Name "
				+ "FROM actions a, matches m, players p "
				+ "WHERE a.PlayerID=p.PlayerID AND m.MatchID=a.MatchID AND m.MatchID=? ";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, m.getMatchID());
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				result.add(player);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	/*
	 *  Sempre considerando la tabella Actions, si definisca 
		l’efficienza ei di un giocatore i all’interno della partita m
		come la somma dei suoi passaggi riusciti (SP, campo 
		totalSuccessfulPassesAll) e dei suoi assists (AS, campo 
		assists), divisa per il numero di minuti giocati dallo stesso
		giocatore (TP, campo timePlayed):
	 * 
	 */
	public List<Player> calcoloEfficienza(Match m){
		String sql = "SELECT p.PlayerID, p.Name, SUM(a.TotalSuccessfulPassesAll) AS sp, SUM(a.Assists) AS sa, a.TimePlayed AS tp "
				+ "FROM actions a, matches m, players p "
				+ "WHERE a.PlayerID=p.PlayerID AND m.MatchID=a.MatchID AND m.MatchID=? "
				+ "GROUP BY p.PlayerID, p.Name ";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, m.getMatchID());
			ResultSet res = st.executeQuery();
			while (res.next()) {
				
				double efficienza = 0.0; 
				int sp = res.getInt("sp");
				int sa = res.getInt("sa");
				int tp = res.getInt("tp");
				efficienza = (sp+sa)/tp;
				
				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				player.setEfficienza(efficienza);
				result.add(player);
			}
			conn.close();
			Collections.sort(result);
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public List<Coppia> getAllCoppie(Match m, Map<Integer, Player> idMapPlayers){
		String sql = "SELECT a1.PlayerID AS p1, a2.PlayerID AS p2 "
				+ "FROM actions a1, actions a2 "
				+ "WHERE a1.PlayerID>a2.PlayerID AND a1.MatchID=a2.MatchID AND a1.MatchID=? AND a1.TeamID<>a2.TeamID ";
		List<Coppia> result = new ArrayList<Coppia>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, m.getMatchID());
			ResultSet res = st.executeQuery();
			while (res.next()) {
				
				double peso = 0.0;
				Player p1 = idMapPlayers.get(res.getInt("p1"));
				Player p2 = idMapPlayers.get(res.getInt("p2"));
				
				if (p1!= null && p2!=null) {
					
					if (p1.getEfficienza()>=p2.getEfficienza()) {
						peso = p1.getEfficienza()-p2.getEfficienza();
						Coppia c = new Coppia(p1, p2, peso);
						result.add(c);
					}else if (p1.getEfficienza()<p2.getEfficienza()) {
						peso = p2.getEfficienza()-p1.getEfficienza();
						Coppia c = new Coppia(p2, p1, peso);
						result.add(c);
					}
				}
			}
			conn.close();
			Collections.sort(result);
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
