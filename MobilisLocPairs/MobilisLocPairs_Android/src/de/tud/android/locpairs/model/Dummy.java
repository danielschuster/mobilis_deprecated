
package de.tud.android.locpairs.model;

import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * The Class Dummy.
 */
public class Dummy {
	
	/** The game1. */
	private Game game1;
	
	/** The team1. */
	private Team team1;
	
	/** The team2. */
	private Team team2;
	
	/** The player1. */
	private Player player1;
	
	/** The player2. */
	private Player player2;
	
	/** The player3. */
	private Player player3;
	
	/** The player4. */
	private Player player4;	
	
	/** The name1. */
	private String name1 = "Hans";
	
	/** The name2. */
	private String name2 = "Lisa";
	
	/** The name3. */
	private String name3 = "Lutz";
	
	/** The name4. */
	private String name4 = "Marie";
	
	/** The id1. */
	private String id1 = "01";
	
	/** The id2. */
	private String id2 = "02";
	
	/** The id3. */
	private String id3 = "03";
	
	/** The id4. */
	private String id4 = "04";
	
	/** The card1. */
	private Card card1;
	
	/** The card2. */
	private Card card2;
	
	/** The pair1. */
	private Pair pair1;
	
	/**
	 * Instantiates a new dummy.
	 */
	public Dummy(){
		
		
		
		player1 = new Player(name1,id1);
		player2 = new Player(name2,id2);
		player3 = new Player(name3,id3);
		player4 = new Player(name4,id4); 

		player1.setPosition(new GeoPosition((int) (51.025258 * 1E6), (int) (13.723948 * 1E6), 0));
		player2.setPosition(new GeoPosition((int) (51.025158 * 1E6), (int) (13.723148 * 1E6), 0));
		player3.setPosition(new GeoPosition((int) (51.025358 * 1E6), (int) (13.723148 * 1E6), 0));
		player4.setPosition(new GeoPosition((int) (51.025458 * 1E6), (int) (13.723748 * 1E6), 0));
		
		/*team1 = new Team(1);
		team1.addPlayer(player1);
		team1.addPlayer(player2);
		
		team2 = new Team(2);
		team2.addPlayer(player3); 
		team2.addPlayer(player4);
		
		card1 = new Card("infe023", "memory11", new GeoPosition(51.025665,13.72333, 0));
		card2 = new Card("infe009", "memory11", new GeoPosition(51.025700,13.723149, 0));

		pair1 = new Pair("http://dummy.com/picture.png",BitmapFactory.decodeResource(LocPairsApp.getContext().getResources(), R.drawable.memory01));
		pair1.addCard(card1);
		pair1.addCard(card2);*/
		
		
		game1 = Game.getInstance();
		//game1.addTeam(team1);
		//game1.addTeam(team2);
		//game1.addPair(pair1);
		game1.setClientPlayer(player1);

	}
	
	
	

}
