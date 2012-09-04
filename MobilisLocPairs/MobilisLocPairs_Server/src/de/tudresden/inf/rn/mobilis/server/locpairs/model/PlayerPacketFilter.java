package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import org.jivesoftware.smack.filter.OrFilter;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.PlayerPacketFilter;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.*;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;


/**
 * The Class PlayerPacketFilter provides a filter specialized for the Player class.
 * If it is used the PlayerPacketListener gets only necessary packets.
 * 
 * @author Reik Mueller
 */
public class PlayerPacketFilter{
	
	private static XMPPBean quitBeanPrototype = new QuitBean();
	private static XMPPBean startGameBeanPrototype = new StartGameBean();
	private static XMPPBean gotThereBeanPrototype = new GoThereBean();
	private static XMPPBean joinGameBeanPrototype = new JoinGameBean();
	private static XMPPBean startRoundBeanPrototype = new StartRoundBean();
	private static XMPPBean uncoverCardBeanPrototype = new UncoverCardBean();
	private static XMPPBean playerUpdateBeanPrototype = new PlayerUpdateBean();
	private static XMPPBean keepAliveBeanPrototype = new KeepAliveBean();
	@SuppressWarnings("unused")
	private static final PlayerPacketFilter instance = new PlayerPacketFilter();
	
	private PlayerPacketFilter(){
		(new BeanProviderAdapter(quitBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(startGameBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(gotThereBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(joinGameBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(startRoundBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(uncoverCardBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(playerUpdateBeanPrototype)).addToProviderManager();
		(new BeanProviderAdapter(keepAliveBeanPrototype)).addToProviderManager();
	}
	
	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public static OrFilter getFilter(){
		return  new OrFilter(	new BeanFilterAdapter(gotThereBeanPrototype),
				new OrFilter(	new BeanFilterAdapter(startRoundBeanPrototype),
				new OrFilter( 	new BeanFilterAdapter(uncoverCardBeanPrototype),
				new OrFilter( 	new BeanFilterAdapter(quitBeanPrototype),
				new OrFilter( 	new BeanFilterAdapter(startGameBeanPrototype),
				new OrFilter(	new BeanFilterAdapter(joinGameBeanPrototype),
				new OrFilter(	new BeanFilterAdapter(keepAliveBeanPrototype),
								new BeanFilterAdapter(playerUpdateBeanPrototype)
				)))))));
	}
	
}
