package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.HighScoreDAO;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Player;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Team;

public class HighscoreDAOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HighScoreDAO dao = new HighScoreDAO();
		Team a = new Team(new Player("nobbi"), new Player("bea"), "id", 1);
		Team d = new Team(new Player("nobbi"), new Player("bea"), "id", 4);
		Team c = new Team(new Player("nobblon"), new Player("beateaton"), "id", 1);
		a.increaseScore();
		c.increaseScore();
		c.increaseScore();
		List<Team> teams = new ArrayList<Team>();
		teams.add(a);
		teams.add(d);
		teams.add(c);

		Collection<Team> test = dao.actualiseHighscore(teams);
		for(Team t : test){
			System.out.println(t.getScore() + " - " + t.getName());
		}
		Map<Long, String> highscores = dao.getHighscore();
		for(Long l : highscores.keySet()){
			System.out.println(highscores.get(l) + " - " + l);
		}
	}

}
