package testimplementation;

import java.util.SortedSet;
import java.util.TreeSet;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.Game;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Player;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Team;

public class Teamtest {
	public static void main(String[] args) {
		SortedSet<Team> teams1 = new TreeSet<Team>();
		SortedSet<Team> teams2 = new TreeSet<Team>();
		Team t1 = new Team(new Player("jid", "name", new Game()), new Player("jid", "name", new Game()), "id", 1);
		Team t2 = new Team(new Player("jid", "name", new Game()), new Player("jid", "name", new Game()), "id", 2);
//		Team t3 = new Team(new Player("jid", "name", new Game()), new Player("jid", "name", new Game()), "id", 2);
		teams1.add(t1);
		teams1.add(t2);
		
		System.out.println(teams1);
		t1.setLastActiveRound(0);
		for(Team t : teams1){
			teams2.add(t);
		}
		teams1 = teams2;
		System.out.println(teams1);
	}
}
