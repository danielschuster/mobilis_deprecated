package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.Team;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.localization.i18n.EclipseLinkLocalizationResource;

/**
 * The Class HighScoreDAO is used to persist high scores of games. 50 high scores are saved.
 * 
 * @author Reik Mueller
 */
public class HighScoreDAO {
	
	private class TeamByScoreComparator implements Comparator<Team>{
		@Override
		public int compare(Team o1, Team o2) {
			if(o1.getScore() > o2.getScore())return -1;
			if(o1.getScore() < o2.getScore())return 1;
			if(o1.getScore() == o2.getScore())return 0;
			return 0;
		}
	}
	private class LongComparator implements Comparator<Long>{
		@Override
		public int compare(Long l1, Long l2) {
			if(l1 > l2)return -1;
			if(l1 < l2)return 1;
			if(l1 == l2)return 0;
			return 0;
		}
	}
	
	private TreeMap<Long, String> highscore = new TreeMap<Long, String>(new LongComparator());
	private List<Team> teams = new ArrayList<Team>(); 
	private static final String PERSISTENCE_UNIT_NAME = "mobilis:locpairs";
	private EntityManagerFactory factory;
	private EntityManager em;
	
	/**
	 * Instantiates a new high score data access object.
	 */
	public HighScoreDAO (){
		
		Map<String, Object> lOptions = new HashMap<String, Object>();
		lOptions.put(PersistenceUnitProperties.JDBC_URL, "jdbc:derby:" + generiereDateipfad("/mobilisLocpairs/highscores/highscoresDb") + ";create=true");
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, lOptions);
		em = factory.createEntityManager();
		Query query = em.createQuery("select t from Team t");
		if (query.getResultList().size() != 0) {
			for (Object o : query.getResultList()) {
				Team p = (de.tudresden.inf.rn.mobilis.server.locpairs.model.Team) o;
				teams.add(p);
			}
		}
		Collections.sort(teams, new TeamByScoreComparator());
	}
	
	/**
	 * Actualize high score.
	 *
	 * @param teams the teams
	 * @return the collection
	 */
	public Collection<Team> actualiseHighscore (Collection<Team> teams){
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		for(Team t : teams){
			em.persist(t);
			this.teams.add(t);
		}
		Collections.sort(this.teams, new TeamByScoreComparator());
		chopAndPersist();
		trans.commit();
		
		return this.teams;
	}
	
	/**
	 * Gets the high score.
	 *
	 * @return the highscore
	 */
	public Map<Long, String> getHighscore(){		
		for(Team t : teams){
			highscore.put(t.getScore(), t.getName());
		}
		return highscore;
	}
	
	private void chopAndPersist(){
		if(teams.size() > 50){
			ArrayList<Team> badTeams = new ArrayList<Team>();
			int i = 0;
			for(Team t : teams){
				if(i > 59){
					badTeams.add(t);
					em.remove(t);
				}
				i++;
			}
			teams.removeAll(badTeams);
		}
		
	}
	
	private String generiereDateipfad(String dateipfadrelativ) {
		String klassenname = this.getClass().getName();
		String klassenpfadrelativ = "/" + klassenname.replace(".", "/")
				+ ".class";
		String klassenpfadabsolut = getClass().getResource(klassenpfadrelativ)
				.getFile();
		String classespfad = klassenpfadabsolut.replace(klassenpfadrelativ, "");
		String buildpfad = classespfad.substring(0, classespfad
				.lastIndexOf("/"));
		String projektpfad = buildpfad.substring(0, buildpfad.lastIndexOf("/"));
		dateipfadrelativ = dateipfadrelativ.replace("\\", "/");
		if (dateipfadrelativ.charAt(0) != '/')
			dateipfadrelativ = "/" + dateipfadrelativ;
		String dateipfadabsolut = projektpfad + dateipfadrelativ;
		dateipfadabsolut = dateipfadabsolut.replace("%20", " ");
		dateipfadabsolut = dateipfadabsolut.substring(1);
		return dateipfadabsolut;
	}
}