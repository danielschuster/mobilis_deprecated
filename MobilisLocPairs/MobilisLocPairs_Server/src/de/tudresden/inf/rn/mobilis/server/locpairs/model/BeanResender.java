package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import java.util.Timer;
import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.BeanResender;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Player;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/**
 * The Class BeanResender is used to periodically resent IQs.
 * 
 * @author Reik Mueller
 */
public class BeanResender extends TimerTask {

	private XMPPBean bean = null;
	private Player player = null;
	private int repetition;
	private int maxRepetition = 5;
	private boolean firstSent = false;
	private static Timer timer = new Timer();

	/**
	 * Instantiates a new bean resender.
	 *
	 * @param bean the bean
	 * @param player the player
	 * @param repetition the repetition
	 */
	public BeanResender(XMPPBean bean, Player player, int repetition) {
		this.bean = bean;
		this.player = player;
		this.repetition = repetition;
		this.firstSent = true;

		if(!firstSent)sendFirstBean();
	}

	/**
	 * Instantiates a new bean resender.
	 *
	 * @param bean the bean
	 * @param player the player
	 */
	public BeanResender(XMPPBean bean, Player player) {
		this.bean = bean;
		this.player = player;
		this.repetition = 0;

		sendFirstBean();
	}
	
	private void sendFirstBean(){
		player.getConnection().sendPacket(new BeanIQAdapter(bean));
		timer.schedule(new BeanResender(bean, player, repetition++),
				new Long(5000));
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		System.out.println(player.isAcknowledged(bean));
		if (!player.isAcknowledged(bean)) {

			if (repetition < maxRepetition) {
				player.getConnection().sendPacket(new BeanIQAdapter(bean));
				timer.schedule(new BeanResender(bean, player, repetition++),
						new Long(5000));
			} else {
				player.beanSendingError(bean);
			}
		}
	}	
}
