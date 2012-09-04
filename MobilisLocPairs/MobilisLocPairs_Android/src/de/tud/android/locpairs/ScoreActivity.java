package de.tud.android.locpairs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Team;
import de.tud.android.mapbiq.R;
import de.tud.android.mapbiq.locator.GpsPositioningService;
import de.tud.android.mapbiq.locator.WifiPositioningService;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Shows the game score of each team and its members at the and of the game. 
 */
public class ScoreActivity extends Activity{
	
	/** The scores. */
	private Map<Integer, Long> scores;
	
	/** The teams. */
	private HashMap<Integer, Team> teams;
	
	/** The score1. */
	private int score1 = 0;
	
	/** The score2. */
	private int score2 = 0;
	
	/** The score counter. */
	private int scoreCounter = 0;
	
	/** The score team1. */
	TextView scoreTeam1;
	
	/** The score team2. */
	TextView scoreTeam2; 
	
	/** The player1_1. */
	TextView player1_1;
	
	/** The player1_2. */
	TextView player1_2;
	
	/** The player2_1. */
	TextView player2_1;
	
	/** The player2_2. */
	TextView player2_2;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locpairs_highscores);
		
		scores = Game.getInstance().getPoints();
		teams = Game.getInstance().getTeams();
		
		player1_1 = (TextView) findViewById(R.id.player1_1);
		player1_2 = (TextView) findViewById(R.id.player1_2);
		player2_1 = (TextView) findViewById(R.id.player2_1);
		player2_2 = (TextView) findViewById(R.id.player2_2);
		
		ImageView imgTeam1 = (ImageView) findViewById(R.id.img1);
		ImageView imgTeam2 = (ImageView) findViewById(R.id.img2);
		
		scoreTeam1 = (TextView) findViewById(R.id.team1_score1);
		scoreTeam2 = (TextView) findViewById(R.id.team2_score2);
		
		imgTeam1.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ingame_droid_green));
		imgTeam2.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ingame_droid_orange));
		
		for(Integer i:scores.keySet()) {
			for(String s:teams.get(i).getPlayers().keySet()) {
				Log.d("scores", "TeamID " + i + " Player " + teams.get(i).getPlayers().get(s).getPlayername());
			}
		}
		
		for(Integer i:scores.keySet()) {
			Log.d("scores", "Team " + i + " Points " + scores.get(i));
		}
		
		Iterator<Integer> sorcesIter = scores.keySet().iterator();
		if(sorcesIter.hasNext()) score1 = scores.get(sorcesIter.next()).intValue();
		if(sorcesIter.hasNext()) score2 = scores.get(sorcesIter.next()).intValue();
				
		Button okButton = (Button) findViewById(R.id.ok_btn);
		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Game.getInstance().reset();
				Intent dashboardIntent = new Intent(getApplicationContext(), LocPairs.class);
				startActivity(dashboardIntent);
				
			}
		
		});
	
		updateScore();
	}
	
	/**
	 * updates score if points have changed.
	 */
	private void updateScore() {
		Iterator<Integer> teamsIter = teams.keySet().iterator();

		if(teamsIter.hasNext()) {
			Integer team = teamsIter.next();
			Iterator<String> playerIter = teams.get(team).getPlayers().keySet().iterator();
			if(playerIter.hasNext()) player1_1.setText(String.valueOf(teams.get(team).getPlayers().get(playerIter.next()).getPlayername()));
			if(playerIter.hasNext()) player1_2.setText(String.valueOf(teams.get(team).getPlayers().get(playerIter.next()).getPlayername()));
		}
		
		if(teamsIter.hasNext()) {
			Integer team = teamsIter.next();
			Iterator<String> playerIter = teams.get(team).getPlayers().keySet().iterator();
			if(playerIter.hasNext()) player2_1.setText(String.valueOf(teams.get(team).getPlayers().get(playerIter.next()).getPlayername()));
			if(playerIter.hasNext()) player2_2.setText(String.valueOf(teams.get(team).getPlayers().get(playerIter.next()).getPlayername()));
		}

		try {
		scoreTeam1.setText(String.valueOf(score1));
		scoreTeam2.setText(String.valueOf(score2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
