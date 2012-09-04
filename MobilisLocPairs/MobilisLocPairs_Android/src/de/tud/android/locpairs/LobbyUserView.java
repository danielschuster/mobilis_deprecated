package de.tud.android.locpairs;

import de.tud.android.locpairs.model.Player;
import de.tud.android.mapbiq.R;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The Class LobbyUserView which provides the GUI of the lobby.
 */
public class LobbyUserView  extends LinearLayout{

	/** The m_vw lobby user text. */
	private TextView m_vwLobbyUserText;
	
	/** The m_vw lobby user team text. */
	private TextView m_vwLobbyUserTeamText;
	
	/** The m_vw lobby user image. */
	private ImageView m_vwLobbyUserImage;
	
	/** The m_lobby user. */
	private Player m_lobbyUser;
 
	/**
	 * Basic Constructor that takes only takes in an application Context.
	 * 
	 * @param context
	 *            The application Context in which this view is being added. 
	 *            
	 * @param lobbyUser
	 * 			  The LobbyUser this view is responsible for displaying.
	 */
	public LobbyUserView(Context context, Player lobbyUser) {
		super(context);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(
				  Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.locpairs_relative_lobbyuser_view, this, true);
		m_vwLobbyUserText = (TextView) this.findViewById(R.id.LobbyUserTextView);
		m_vwLobbyUserTeamText = (TextView) this.findViewById(R.id.LobbyUserTeamTextView);
		m_vwLobbyUserImage = (ImageView) findViewById(R.id.LobbyUserImage);
		setLobbyUser(lobbyUser);
		requestLayout();
	}
	

	/**
	 * Mutator method for changing the LobbyUser object this View displays. This View
	 * will be updated to display the correct contents of the new LobbyUser.
	 * 
	 * @param lobbyUser
	 *            The LobbyUser object which this View will display.
	 */
	public void setLobbyUser(Player lobbyUser) {
		m_lobbyUser = lobbyUser;
		m_vwLobbyUserText.setText(m_lobbyUser.getPlayername());
		if(m_lobbyUser.getTeam().getTeamID()!=-1) {
			m_vwLobbyUserTeamText.setText("Team " + m_lobbyUser.getTeam().getTeamID());
		} else {
			m_vwLobbyUserTeamText.setText("waiting for teammate");
		}
		Log.v("add Player in Lobby", m_lobbyUser.getPlayername() + ", Team " + m_lobbyUser.getTeam().getTeamID() );
		m_vwLobbyUserText.setTextSize(14);
		m_vwLobbyUserTeamText.setTextSize(14);
		
		
		if (lobbyUser.getTeam().getTeamID() == 1){
			m_vwLobbyUserImage.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_green));
		}
		if (lobbyUser.getTeam().getTeamID() == 2){
			m_vwLobbyUserImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.ingame_droid_orange));
		}
		if (lobbyUser.getTeam().getTeamID() == 3){
			m_vwLobbyUserImage.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_blue));
		}
		if (lobbyUser.getTeam().getTeamID() == -1){
			m_vwLobbyUserImage.setBackgroundDrawable(getResources().getDrawable( R.drawable.ingame_droid_red));
		}
	
	}

}
