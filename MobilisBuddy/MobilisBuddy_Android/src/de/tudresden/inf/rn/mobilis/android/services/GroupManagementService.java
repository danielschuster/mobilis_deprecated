package de.tudresden.inf.rn.mobilis.android.services;

import java.util.HashMap;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.MainView;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.android.util.Util;
import de.tudresden.inf.rn.mobilis.xmpp.packet.CreateGroupIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.JoinGroupIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.QueryGroupsIQ;
import de.tudresden.inf.rn.mobilis.xmpp.provider.CreateGroupIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.JoinGroupIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.QueryGroupsIQProvider;

public class GroupManagementService implements InvitationListener {

	private static final String TAG = "GroupManagementService";
	XMPPConnection mConnection;
	private boolean mInGroup;
	private String mSessionAgent;
	private String mGroupName;
	private MultiUserChat mMuc;
	private BroadcastReceiver ir;

	public void initIntentReceiver() {
	    ir = new IntentReceiver();
	    Context context = SessionService.getInstance().getContext();
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "servicecall.groupsquery"));
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "servicecall.creategroup"));
	    context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "servicecall.joingroup"));
	}

	public void unregisterIntentReceiver() {
	    SessionService.getInstance().getContext().unregisterReceiver(ir);
	}

	private class IntentReceiver extends BroadcastReceiver {
	    /**
	     * Listen to all service call intents this service gets from the upper
	     * layer.
	     * 
	     * @author István
	     * 
	     */
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (action
	                .equals(Const.INTENT_PREFIX + "servicecall.groupsquery")) {
	            // Receive the groupItems from the Basic Service and forward them
	            // to the upper layers that are interested in them.
	            Thread t = new Thread(new Runnable() {
	                public void run() {
	                    SessionService s = SessionService.getInstance();
	                    HashMap<String, String> gos = getGroupsOnServer();
	                    if (gos != null) {
	                        Intent i = new Intent(Const.INTENT_PREFIX + "callback.groupsquery");
	                        i.putExtra(Const.INTENT_PREFIX + "callback.groupsquery.groups", gos);
	                        s.getContext().sendBroadcast(i);
	                    }
	                }
	            });
	            t.setName("QueryGroupsThread");
	            t.start();
	        } else if (action.equals(Const.INTENT_PREFIX + "servicecall.creategroup")) {
	            String groupName = intent
	            .getStringExtra(Const.INTENT_PREFIX + "servicecall.creategroup.groupname");
	            createGroup(groupName);
	        } else if (action.equals(Const.INTENT_PREFIX + "servicecall.joingroup")) {
	            String groupName = intent
	            .getStringExtra(Const.INTENT_PREFIX + "servicecall.joingroup.groupname");
	            joinGroup(groupName);
	        }
	    }
	}

	public String getSessionAgent() {
	    return mSessionAgent;
	}

	public void setSessionAgent(String sessionAgent) {
		mSessionAgent = sessionAgent;
	}

	public String getGroupName() {
		return mGroupName;
	}

	public void setGroupName(String groupName) {
		mGroupName = groupName;
	}

	public boolean isInGroup() {
		return mInGroup;
	}

	public void setInGroup(boolean inGroup) {
		mInGroup = inGroup;
	}

	public MultiUserChat getMuc() {
		return mMuc;
	}

	public void initialize(XMPPConnection conn) {
		mConnection = conn;
		MultiUserChat.addInvitationListener(conn, this);

		ProviderManager pm = ProviderManager.getInstance();
		pm.addIQProvider(QueryGroupsIQ.elementName, QueryGroupsIQ.namespace,
				new QueryGroupsIQProvider());
		pm.addIQProvider(CreateGroupIQ.elementName, CreateGroupIQ.namespace,
				new CreateGroupIQProvider());
		pm.addIQProvider(JoinGroupIQ.elementName, JoinGroupIQ.namespace,
				new JoinGroupIQProvider());

		mInGroup = false;
	}

	/**
	 * Gets the group id of the group the specified session agent is responsible
	 * for.
	 * 
	 * @param saJid
	 *            The bare jid of the session agent.
	 * @return The id of the group (MUC).
	 */
	public String getGroupOfAgent(String saJid) {
		// TODO support multi groups
		return getGroupName();
	}

	/**
	 * Asks the Session Coordinator for a list of all available groups.
	 * 
	 * @return A HashMap with Session Agent, groupname pairs.
	 */
	public HashMap<String, String> getGroupsOnServer() {
		PacketCollector pc = mConnection
				.createPacketCollector(new PacketTypeFilter(QueryGroupsIQ.class));

		QueryGroupsIQ qgi = new QueryGroupsIQ();
		qgi.setType(Type.GET);
		qgi.setFrom(mConnection.getUser());
		qgi.setTo(SessionService.getInstance().getCoordinator());
		mConnection.sendPacket(qgi);

		final Context context = SessionService.getInstance().getContext();
		long timeout = SessionService.getInstance().getPreferences().getLong(
                "pref_xmpp_timeout", 10000);
		MainView.getMainThreadHandler().post(new Runnable() {
		    @Override
		    public void run() {
		        ProgressDialog progressDialog = ProgressDialog.show(context, null,
		                context.getResources().getString(R.string.querygroups));
		        MainView.setProgressDialog(progressDialog);
		    }
		});
		
	    Log.d(TAG, "Sent packet: QueryGroupsIQ (GET)");
	    // waiting for response IQ with existing groups
		qgi = (QueryGroupsIQ) pc.nextResult(timeout);
		
		MainView.getMainThreadHandler().post(new Runnable() {
		    @Override
		    public void run() {
		        MainView.getProgressDialog().dismiss();
		    }
		});
		
		if (qgi == null) {
		    Log.w(TAG, "Timeout waiting for packet: QueryGroupsIQ (RESULT)");
	        MainView.getMainThreadHandler().post(new Runnable() {
	            @Override
	            public void run() {
	                Util.showAlertDialog(context, 
	                        context.getResources().getString(R.string.dlg_title_noresponse), 
	                        context.getResources().getString(R.string.querygroups_failed));
	            }
	        });
		    return null;
		} else {
		    Log.d(TAG, "Received packet: QueryGroupsIQ (RESULT)");
		    return qgi.getGroups();
		}
	}

	/**
	 * Creates a group on the server.
	 * 
	 * @param groupName
	 *            The name of the new group.
	 */
	public void createGroup(String groupName) {
		// TODO look up if current server implementation returns result at all.
		// PacketCollector pc = mConnection
		// .createPacketCollector(new PacketTypeFilter(CreateGroupIQ.class));

		CreateGroupIQ cgi = new CreateGroupIQ();
		cgi.setFrom(mConnection.getUser());
		cgi.setTo(SessionService.getInstance().getCoordinator());
		cgi.setGroup(groupName);
		mConnection.sendPacket(cgi);

		// cgi = (CreateGroupIQ) pc.nextResult();
		appCallbackCreateGroup();
	}

	/**
	 * Joins a group on the server.
	 * 
	 * @param groupName
	 *            The name of the group to join.
	 */
	public void joinGroup(String groupName) {
		JoinGroupIQ jgi = new JoinGroupIQ();
		jgi.setFrom(mConnection.getUser());
		jgi.setTo(SessionService.getInstance().getCoordinator());
		jgi.setGroup(groupName);
		mConnection.sendPacket(jgi);
	}

	/**
	 * Sends intent that create group was successful.
	 */
	private void appCallbackCreateGroup() {
		Intent i = new Intent(
				Const.INTENT_PREFIX + "callback.creategroup");
		SessionService.getInstance().getContext().sendBroadcast(i);
	}

	/**
	 * Sends intent about joining group success.
	 */
	private void appCallbackJoinGroup() {
		// notify LoginView
		Intent i = new Intent(
				Const.INTENT_PREFIX + "callback.joingroup");
		SessionService.getInstance().getContext().sendBroadcast(i);
	}

	/**
	 * END WRAPPER FOR UIL
	 */

	/**
	 * START WRAPPER FOR BASIC SERVICE LAYER
	 */

	/**
	 * Receives and processes invitations that are sent out by the Session
	 * Agent. Also reads out the Session Agent's JID.
	 */
	@Override
	public void invitationReceived(XMPPConnection conn, String room,
			String inviter, String reason, String password, Message msg) {
		// join room
		mMuc = new MultiUserChat(conn, room);

		String user = conn.getUser();
		try {
			SessionService sessService = SessionService.getInstance();

			if (!sessService.getGroupManagement().isInGroup()) {
				mMuc.join(StringUtils.parseName(user));
				mMuc.addMessageListener(SessionService.getInstance()
						.getMessageHandler());

				mInGroup = true;
				mGroupName = room.substring(0, room.indexOf("@"));
				mSessionAgent = inviter;

				appCallbackJoinGroup();
			}
		} catch (XMPPException e) {
			// TODO throw custom exception
			e.printStackTrace();
		}
	}

	/**
	 * END WRAPPER FOR BSL
	 */
}
