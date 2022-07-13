package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMapPlayers;
	
	public Model() {
		this.dao = new PremierLeagueDAO();
	}
	
	
	public List<Match> getAllMatches(){
		return this.dao.listAllMatches();
	}
	
	public void creaGrafo(Match m) {
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		this.idMapPlayers = new HashMap<>();
		
		//creo una mappa con tutti i giocatori aggiunti 
		for (Player p : this.dao.getAllVertici(m)) {
			idMapPlayers.put(p.getPlayerID(), p);
		}
		
		Graphs.addAllVertices(this.grafo, this.dao.getAllVertici(m));
		
		//per aggiungere gli archi
		for (Coppia c : this.dao.getAllCoppie(m, idMapPlayers)) {		
			this.grafo.addEdge(c.getP1(), c.getP2());
			this.grafo.setEdgeWeight(c.getP1(), c.getP2(), c.getPeso());
		}
		
	}
	public String getGiocatoreMigliore() {
		double max = 0.0;
		double efficienzaTot;
		double sommaEntranti;
		double sommaUscenti;
		Player scelto = new Player(0, null);
		
		
		for(Player p : this.grafo.vertexSet()) {
			 efficienzaTot = 0.0;
			 sommaEntranti = 0.0;
			 sommaUscenti = 0.0;
			for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p)) {
				sommaUscenti += this.grafo.getEdgeWeight(e);
			}
			for (DefaultWeightedEdge e : this.grafo.incomingEdgesOf(p)) {
				sommaEntranti+= this.grafo.getEdgeWeight(e);
			}
			efficienzaTot=sommaUscenti-sommaEntranti;
			if (efficienzaTot>=max) {
				max=efficienzaTot;
				scelto = p;
			}
		}
		double ris = Math.round(max*1000.00)/1000.00;
		return scelto.name+ " "+ ris;
	}
	public Integer nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public Integer nArchi() {
		return this.grafo.edgeSet().size();
	}

}
