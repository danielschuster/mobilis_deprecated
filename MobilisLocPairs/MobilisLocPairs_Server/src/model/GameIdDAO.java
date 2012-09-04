package model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import model.PersistentGameId;

/**
 * The Class GameIdDAO. It is used to provide access to objects of the TypePersistentGameId
 * 
 * @author Reik Mueller
 */
public class GameIdDAO {
	
	private static final String PERSISTENCE_UNIT_NAME = "mobilis:locpairs";
	private EntityManagerFactory factory;
	private EntityManager em;
	private PersistentGameId gameId = null;
	
	/**
	 * Instantiates a new game id dao.
	 */
	public GameIdDAO() {
		Map<String, Object> lOptions = new HashMap<String, Object>();
		lOptions.put(PersistenceUnitProperties.JDBC_URL, "jdbc:derby:" + generiereDateipfad("/mobilis/locpairs/highscores/highscoresDb") + ";create=true");
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, lOptions);
		em = factory.createEntityManager();
		gameId = new PersistentGameId();
		em.getTransaction().begin();
		em.persist(gameId);
		em.getTransaction().commit();
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public PersistentGameId getId(){	
		return gameId;
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
