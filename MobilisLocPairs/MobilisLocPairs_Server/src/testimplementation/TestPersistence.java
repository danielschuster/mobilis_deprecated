package testimplementation;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.GeoPoint;

public class TestPersistence {

	private static final String PERSISTENCE_UNIT_NAME = "networkfingerprints";
	private static EntityManagerFactory factory;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();
		
		// Erzeugen von GeoPoint-einträgen
		em.getTransaction().begin();
		for(int i = 0; i < 50; i++){
			GeoPoint gp = new GeoPoint(i, i);
			em.persist(gp);
		}
		em.getTransaction().commit();
		
		// Lesen von GeoPoint-einträgen
		em.getTransaction().begin();
		Query q = em.createQuery("select m from GeoPoint m");
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		for(Object o : q.getResultList()){
			GeoPoint gp1 = (GeoPoint)o;
			System.out.println(gp1.toString());
		}
		em.getTransaction().commit();
		em.close();
	}
}
