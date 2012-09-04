package de.tud.android.locpairs.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import de.tud.android.helpers.Parceller;
import de.tud.android.helpers.SntpClient;
import de.tud.android.locpairs.model.Card;
import de.tud.android.locpairs.model.Game;
import de.tud.android.locpairs.model.Instance;
import de.tud.android.locpairs.model.Pair;
import de.tud.android.locpairs.model.Player;
import de.tud.android.locpairs.model.Round;
import de.tud.android.locpairs.model.Settings;
import de.tud.android.locpairs.model.Team;
import de.tud.android.mapbiq.LocPairsApp;
import de.tud.android.mapbiq.R;
import de.tud.android.mapbiq.loader.WFSConnector;
import de.tud.server.model.LocationModelAPI;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.EndGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GameInformationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GoThereBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.KeepAliveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.PlayerUpdateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.QuitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.ShowCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.UncoverCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.LocPairsDateFormat;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;

/**
 * The Class LocPairsController handles communication between server and client.
 */
public class LocPairsController extends Service {

	/** The TAG for the Log. */
	private final static String TAG = "LocPairsController";

	/** Flag for indicating if MXA is connected. */
	private boolean xmppReady = false;

	/** MXA Controller. */
	private MXAController mMXAController;

	/** XMPP Service. */
	private IXMPPService iXMPPService;

	/** Application Context. */
	private Context ctx;

	/** The binder. */
	private final Binder binder = new LocalBinder();

	/** The game service intent. */
	private Intent gameServiceIntent;

	/** Gaming Client, responsible for client-side Gamelogic. */
	private GamingClient gamingClient;

	/** Handler for timed actions. */
	private Handler timerHandler = new Handler();

	/**
	 * The startGameBean. When StartGameIQ is acknowledged a StartRoundIQ
	 * follows immediately - this causes a problem when InRoundPlayingActivity
	 * is not started yet. So the StartGameBean is stored here and a Method for
	 * acknowledging is called by InRoundPlayingActivity.
	 */
	private XMPPBean startGameBean;

	/** Offset: LocalDeviceTime - time retrieved via ntp. */
	private long timeOffset = 0;

	/** The indicator if game is started. */
	private long started;

	/** The number of open instances on mobilis server. */
	private int currentInstancesCount = 0;

	/** List of beans that are waiting for acknowledgements. */
	private Map<String, XMPPBean> beansWaitingForAck = new HashMap<String, XMPPBean>();

	/** Beans that could not be sent due to interrupted network connections. */
	private Map<String, XMPPBean> beansToBeSent = new HashMap<String, XMPPBean>();

	/**
	 * Map for Debugging Purposes. Beans in this Map will produce Debug Output.
	 * The String defines the Class name, the integer the expected bean type.
	 */
	HashMap<String, Integer> debugBeans = new HashMap<String, Integer>();
	
	/**
	 * True means callbacks produce Debug Output when they are called
	 */
	boolean viewCallBackDebugOutput = false;

	/**
	 * String Representation of XMPPBean Types
	 */
	String[] beanTypes = {"SET","GET","RESULT","ERROR"};
	
	/** Sets the Interval for KeepAliveBeans */
	protected int keepAliveDelay = 10000;
	
	/**
	 * Initialize MXA and register XMPP Beans with Parceller.
	 * 
	 * 
	 */
	public void initializeMXA() {
		// Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
		ctx = getApplicationContext();
		// activity.startActivity(Intent.createChooser(i,
		// "MXA not found. Please install."));

		JoinGameBean prototype1 = new JoinGameBean();
		GoThereBean prototype2 = new GoThereBean();
		StartRoundBean prototype3 = new StartRoundBean();
		QuitBean prototype4 = new QuitBean();
		UncoverCardBean prototype5 = new UncoverCardBean();
		StartGameBean prototype6 = new StartGameBean();
		EndGameBean prototype7 = new EndGameBean();
		KeepAliveBean prototype8 = new KeepAliveBean();
		PlayerUpdateBean prototype9 = new PlayerUpdateBean();
		ShowCardBean prototype10 = new ShowCardBean();
		MobilisServiceDiscoveryBean prototype11 = new MobilisServiceDiscoveryBean();
		GameInformationBean prototype12 = new GameInformationBean();
		CreateNewServiceInstanceBean prototype13 = new CreateNewServiceInstanceBean();

		Parceller.getInstance().registerXMPPBean(prototype1);
		Parceller.getInstance().registerXMPPBean(prototype2);
		Parceller.getInstance().registerXMPPBean(prototype3);
		Parceller.getInstance().registerXMPPBean(prototype4);
		Parceller.getInstance().registerXMPPBean(prototype5);
		Parceller.getInstance().registerXMPPBean(prototype6);
		Parceller.getInstance().registerXMPPBean(prototype7);
		Parceller.getInstance().registerXMPPBean(prototype8);
		Parceller.getInstance().registerXMPPBean(prototype9);
		Parceller.getInstance().registerXMPPBean(prototype10);
		Parceller.getInstance().registerXMPPBean(prototype11);
		Parceller.getInstance().registerXMPPBean(prototype12);
		Parceller.getInstance().registerXMPPBean(prototype13);

		prepareDebug();
		
		disconnect();
		mMXAController = MXAController.get();
		mMXAController.connectMXA(ctx, mMXAListener);

		gameServiceIntent = new Intent(ctx, GamingClient.class);
		bindService(gameServiceIntent, onService, Context.BIND_AUTO_CREATE);

		Log.v("MXA", "connected");
	}

	/** Every Bean in List debugBeans will produce additional Debug Output. */
	private void prepareDebug() {
		debugBeans.put(JoinGameBean.class.getName(), XMPPBean.TYPE_RESULT);
		debugBeans.put(GoThereBean.class.getName(), XMPPBean.TYPE_SET);
		debugBeans.put(StartRoundBean.class.getName(), XMPPBean.TYPE_SET);
		debugBeans.put(QuitBean.class.getName(), XMPPBean.TYPE_SET);
		debugBeans.put(UncoverCardBean.class.getName(), XMPPBean.TYPE_RESULT);
		debugBeans.put(StartGameBean.class.getName(), XMPPBean.TYPE_SET);
		debugBeans.put(EndGameBean.class.getName(), XMPPBean.TYPE_SET);
		// debugBeans.put(KeepAliveBean.class.getName(), XMPPBean.TYPE_RESULT);
		// debugBeans.put(PlayerUpdateBean.class.getName(), XMPPBean.TYPE_RESULT);
		debugBeans.put(ShowCardBean.class.getName(), XMPPBean.TYPE_SET);
		debugBeans.put(MobilisServiceDiscoveryBean.class.getName(),
				XMPPBean.TYPE_RESULT);
		debugBeans.put(GameInformationBean.class.getName(),
				XMPPBean.TYPE_RESULT);
		debugBeans.put(CreateNewServiceInstanceBean.class.getName(),
				XMPPBean.TYPE_RESULT);
	}

	/**
	 * Timer Runnable, updates Player Location and checks if connection still
	 * persists.
	 */
	private Runnable timerRunnableReconnect = new Runnable() {
		public void run() {

			// TODO:
			// Find a way to only send keepAliveBeans when in lobby. (Being in
			// game = to late, always when connected = to early)

			if (xmppReady) {
				if (LocationModelAPI.getWifiCoordinate() != null && LocationModelAPI.getWifiCoordinate().getLongitude()!=Double.NaN) {
					sendKeepAliveMessage(Game.getInstance().getClientPlayer(),
							new GeoPosition(LocationModelAPI
									.getWifiCoordinate().getLatitude(),
									LocationModelAPI.getWifiCoordinate()
											.getLongitude(), 0));
				} else if (LocationModelAPI.getGpsCoordinate() != null) {
					sendKeepAliveMessage(Game.getInstance().getClientPlayer(),
							new GeoPosition(LocationModelAPI.getGpsCoordinate()
									.getLatitude(), LocationModelAPI
									.getGpsCoordinate().getLongitude(), 0));
				} else {
					sendKeepAliveMessage(Game.getInstance().getClientPlayer(),
							null);
				}
			}

			try {
				if (!isOnline() || iXMPPService == null
						|| !iXMPPService.isConnected()) {

					xmppReady = false;
					if (iXMPPService != null) {
						iXMPPService.disconnect(null);
					}
					initializeMXA();

				} else {
					Log.v(TAG, "Still running... Started " + started);
				}
			} catch (RemoteException e) {
				Log.e(TAG, "XMPP Service could not (re)connect.");
				e.printStackTrace();
			}

			/*
			 * Now register it for running next time
			 */
			timerHandler.postDelayed(this, keepAliveDelay);
		}

	};

	/**
	 * Disconnect from XMPP Server and remove Timer Callbacks.
	 */
	public void disconnect() {
		try {
			timerHandler.removeCallbacks(timerRunnableReconnect);
			if(MXAController.get() != null) MXAController.get().getXMPPService().disconnect(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The MXA Connect listener, called when Connection to MXA is established.
	 * Responsible for connecting to XMPP Server
	 */
	private MXAListener mMXAListener = new MXAListener() {
		public void onMXAConnected() {
			iXMPPService = mMXAController.getXMPPService();
			Toast.makeText(ctx, "MXA connected", Toast.LENGTH_SHORT).show();
			try {
				iXMPPService.connect(myConnectMessenger);
				Log.v(TAG, "mMXAListener --> connect method called");
			} catch (RemoteException e) {
				Log.e(TAG, "mMXAListener --> Error connecting to XMPP Server");
				Toast.makeText(ctx, e.toString(), Toast.LENGTH_SHORT).show();
			}
		}

		public void onMXADisconnected() {

		}
	};

	/**
	 * The my XMPP Connect Messenger - called when Connection to XMPP Server is
	 * established. Register Bean Callbacks.
	 */
	private Messenger myConnectMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.v("Test", "myConnectMessenger --> handleMessage");
			// Toast.makeText(ctx, "XMPP connected", Toast.LENGTH_SHORT);

			try {
				Log.v(TAG, "(Re)connected...");

				// Register Bean Callbacks... (Necessary for every Bean)
				iXMPPService.registerIQCallback(joinGameBeanCallback,
						JoinGameBean.CHILD_ELEMENT, JoinGameBean.NAMESPACE);
				iXMPPService.registerIQCallback(goThereBeanCallback,
						GoThereBean.CHILD_ELEMENT, GoThereBean.NAMESPACE);
				iXMPPService.registerIQCallback(startRoundBeanCallback,
						StartRoundBean.CHILD_ELEMENT, StartRoundBean.NAMESPACE);
				iXMPPService.registerIQCallback(quitBeanCallback,
						QuitBean.CHILD_ELEMENT, QuitBean.NAMESPACE);
				iXMPPService.registerIQCallback(uncoverCardBeanCallback,
						UncoverCardBean.CHILD_ELEMENT,
						UncoverCardBean.NAMESPACE);
				iXMPPService.registerIQCallback(startGameBeanCallback,
						StartGameBean.CHILD_ELEMENT, StartGameBean.NAMESPACE);
				iXMPPService.registerIQCallback(endGameBeanCallback,
						EndGameBean.CHILD_ELEMENT, EndGameBean.NAMESPACE);
				iXMPPService.registerIQCallback(keepAliveBeanCallback,
						KeepAliveBean.CHILD_ELEMENT, KeepAliveBean.NAMESPACE);
				iXMPPService.registerIQCallback(playerUpdateBeanCallback,
						PlayerUpdateBean.CHILD_ELEMENT,
						PlayerUpdateBean.NAMESPACE);
				iXMPPService.registerIQCallback(showCardBeanCallback,
						ShowCardBean.CHILD_ELEMENT, ShowCardBean.NAMESPACE);
				iXMPPService.registerIQCallback(
						mobilisServiceDiscoveryBeanCallback,
						MobilisServiceDiscoveryBean.CHILD_ELEMENT,
						MobilisServiceDiscoveryBean.NAMESPACE);
				iXMPPService.registerIQCallback(gameInformationBeanCallback,
						GameInformationBean.CHILD_ELEMENT,
						GameInformationBean.NAMESPACE);
				iXMPPService.registerIQCallback(
						createNewServiceInstanceBeanCallback,
						CreateNewServiceInstanceBean.CHILD_ELEMENT,
						CreateNewServiceInstanceBean.NAMESPACE);

				setTimeOffset();
				Game.getInstance().getClientPlayer()
						.setPlayerID(iXMPPService.getUsername());
				Game.getInstance().addPlayer(
						Game.getInstance().getClientPlayer());

				xmppReady = true;

				// Send Beans that could not be sent due to connection loss
				for (XMPPBean b : beansToBeSent.values()) {
					iXMPPService.sendIQ(new Messenger(new Handler() {
						public void handleMessage(Message msg) {
							Log.v(TAG, "Waiting IQ sent.");
						}
					}), null, 1,
							Parceller.getInstance()
									.convertXMPPBeanToIQ(b, true));

					beansToBeSent.remove(b);
					beansWaitingForAck.put(b.getId(), b);
				}

				// Get initial Coordinates
				if (LocationModelAPI.getGpsCoordinate() != null) {
					sendKeepAliveMessage(Game.getInstance().getClientPlayer(),
							new GeoPosition(LocationModelAPI.getGpsCoordinate()
									.getLatitude(), LocationModelAPI
									.getGpsCoordinate().getLongitude(), 0));
				} else if (LocationModelAPI.getWifiCoordinate() != null) {
					sendKeepAliveMessage(Game.getInstance().getClientPlayer(),
							new GeoPosition(LocationModelAPI
									.getWifiCoordinate().getLatitude(),
									LocationModelAPI.getWifiCoordinate()
											.getLongitude(), 0));
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
	});

	// ------------------- Send XMPP Message Methods ------------------- //

	/**
	 * Send InformationQuery. Basic Method that is used by following
	 * send<..>Message Methods
	 * 
	 * @param b
	 *            the Bean
	 * @param to
	 *            the Recipient
	 * @param type
	 *            GET, SET or RESULT
	 * @return true, if successful
	 */
	private boolean sendIQ(XMPPBean b, String to, int type) {
		String TAG = "LocPairsController:SentBeans"; 
		if (xmppReady)
			try {
				b.setTo(to);
				b.setType(type);
				b.setFrom(iXMPPService.getUsername());

				if (debugBeans.containsKey(b.getClass().getName())) {
					Log.v(TAG, "--- " + b.getClass().getName()
							+ " ---");
					Log.v(TAG, "ID : " + b.getId());
					Log.v(TAG, "Type : " + beanTypes[b.getType()]);
					Log.v(TAG, "From : " + b.getFrom());
					Log.v(TAG, "To : " + b.getTo());
					Log.v(TAG, "Payload : " + b.toXML());
				}
				iXMPPService.sendIQ(new Messenger(iqSentHandler), null, 1,
						Parceller.getInstance().convertXMPPBeanToIQ(b, true));

				Game.getInstance().setReConnect(false);
				if (b.getType() != XMPPBean.TYPE_RESULT)
					beansWaitingForAck.put(b.getId(), b);
				return true;
			} catch (RemoteException e) {

				e.printStackTrace();
				return false;
			}

		else {
			beansToBeSent.put(b.getId(), b);
			Log.e("XMPP_ERROR", "XMPP Service not ready.");
			return false;
		}
	}

	/**
	 * Send acknowledgement.
	 * 
	 * @param b
	 *            the Bean which should be acknowledged
	 * @return true, if successful
	 */
	public boolean sendAck(XMPPBean b) {
		if (b instanceof EndGameBean) {
			((EndGameBean) b).setResult(true);
		} else if (b instanceof GoThereBean) {
			((GoThereBean) b).setResult(true);
		} else if (b instanceof JoinGameBean) {
			((JoinGameBean) b).setResult(true);
		} else if (b instanceof KeepAliveBean) {
			((KeepAliveBean) b).setResult(true);
		} else if (b instanceof PlayerUpdateBean) {
			((PlayerUpdateBean) b).setResult(true);
		} else if (b instanceof QuitBean) {
			((QuitBean) b).setResult(true);
		} else if (b instanceof ShowCardBean) {
			((ShowCardBean) b).setResult(true);
		} else if (b instanceof GameInformationBean) {
			((GameInformationBean) b).setResult(true);
		}

		try {
			b.setFrom(iXMPPService.getUsername());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return sendIQ(b, Game.getInstance().getGameID(), XMPPBean.TYPE_RESULT);
	}

	Handler iqSentHandler = new Handler() {
		public void handleMessage(Message msg) {
			// Log.v(TAG, "IQ sent.");
		}
	};

	/**
	 * Send service discovery request. Response contains list of all open
	 * instances.
	 * 
	 * @return true, if successful
	 */
	public boolean sendDiscoveryRequest() {
		return sendIQ(new MobilisServiceDiscoveryBean(Settings.getInstance()
				.getLocPairsNamespace(), null), Settings.getInstance()
				.getMobilisAddress(), XMPPBean.TYPE_GET);
	}

	/**
	 * Send game information message. Response contains informations about the
	 * corresponding gaming instance.
	 * 
	 * @param gameJID
	 *            the XMPP ID of the specific fame instance
	 * @return true, if successful
	 */
	public boolean sendGameInformationMessage(String gameJID) {
		return sendIQ(new GameInformationBean(), gameJID, XMPPBean.TYPE_SET);
	}

	/**
	 * Send create game message. Creates new instance of LocPairs. Response will
	 * be sent from corresponding Game XMPP ID
	 * 
	 * @return true, if successful
	 */
	public boolean sendCreateGameMessage() {
		return sendIQ(new CreateNewServiceInstanceBean(Settings.getInstance()
				.getLocPairsNamespace(), null), Settings.getInstance()
				.getMobilisAddress(), XMPPBean.TYPE_SET);
	}

	/**
	 * Send join game message. Request to join game specified by Recipient XMPP
	 * Id
	 * 
	 * @param player
	 *            the client player
	 * @param gameJID
	 *            the game XMPP ID
	 * @return true, if successful
	 */
	public boolean sendJoinGameMessage(Player player, String gameJID) {
		Game.getInstance().setGameID(gameJID);
		return sendIQ(
				new JoinGameBean(player.getPosition(), player.getPlayerID(),
						player.getPlayername()), gameJID, XMPPBean.TYPE_SET);
	}

	/**
	 * Send quit message. Leave current game instance.
	 * 
	 * @param player
	 *            the client player
	 * @return true, if successful
	 */
	public boolean sendQuitMessage(Player player) {
		return sendIQ(new QuitBean(player.getPlayerID()), Game.getInstance()
				.getGameID(), XMPPBean.TYPE_SET);
	}

	/**
	 * Send uncover card message.
	 * 
	 * @param card
	 *            the uncovered card
	 * @param player
	 *            the client player
	 * @param fingerprint
	 *            the fingerprint
	 * @return true, if successful
	 */
	public boolean sendUncoverCardMessage(Card card, Player player,
			NetworkFingerPrint fingerprint) {
		long timestamp = System.currentTimeMillis() + timeOffset;
		return sendIQ(new UncoverCardBean(card.getBarcode(), fingerprint,
				LocPairsDateFormat.getFormat().format(new Date(timestamp)),
				player.getPlayerID()), Game.getInstance().getGameID(),
				XMPPBean.TYPE_SET);
	}

	/**
	 * Send go there message.
	 * 
	 * @param player
	 *            the corresponding player (teammate)
	 * @param position
	 *            the position to which the player should go
	 * @return true, if successful
	 */
	public boolean sendGoThereMessage(Player player, GeoPosition position) {
		if (player == null) {
			for (Player player_ : Game.getInstance().getClientPlayer()
					.getTeam().getPlayers().values()) {
				if (player_ != Game.getInstance().getClientPlayer())
					player = player_;
			}
		}
		return sendIQ(new GoThereBean(player.getPlayerID(), position), Game
				.getInstance().getGameID(), XMPPBean.TYPE_SET);
	}

	/**
	 * Send keep alive message.
	 * 
	 * @param player
	 *            the client player
	 * @param position
	 *            the most recent position
	 * @return true, if successful
	 */
	public boolean sendKeepAliveMessage(Player player, GeoPosition position) {
		return sendIQ(new KeepAliveBean(player.getPlayerID(), position), Game
				.getInstance().getGameID(), XMPPBean.TYPE_SET);
	}

	// ------------------- XMPP Callbacks ------------------- //

	private void callbackDebugOutput(XMPPBean b) {
		String TAG = "LocPairsController:RecievedBeans";
		if (debugBeans.containsKey(b.getClass().getName())) {
			if (debugBeans.get(b.getClass().getName()) == b.getType()
					|| b.getType() == XMPPBean.TYPE_RESULT) {
				Log.v(TAG, "--- " + b.getClass().getName()
						+ " ---");
				Log.v(TAG, "Id : " + b.getId());
				Log.v(TAG, "Type : " + beanTypes[b.getType()]);
				Log.v(TAG, "From : " + b.getFrom());
				Log.v(TAG, "To : " + b.getTo());
				Log.v(TAG, "Payload : " + b.toXML());
			} else {
				Log.e(TAG, "--- Bean from Class " + b.getClass().getName()
						+ " arrived. Should not happen. ---");
				Log.e(TAG, "Id : " + b.getId());
				Log.e(TAG, "Type : " + beanTypes[b.getType()]);
				Log.e(TAG, "From : " + b.getFrom());
				Log.e(TAG, "To : " + b.getTo());
				Log.e(TAG, "Payload : " + b.toXML());
				if (b.getType() == XMPPBean.TYPE_ERROR)
					Log.e(TAG, "ErrorType: " + b.errorType
							+ " ErrorCondition: " + b.errorCondition
							+ " ErrorText:" + b.errorText);
			}
		}
	}

	/**
	 * The MobilisServiceDiscoveryBean callback. Bean from type result expected.
	 * Contains list of string arrays that contain game infos.
	 */
	IXMPPIQCallback mobilisServiceDiscoveryBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "mobilisServiceDiscoveryBeanCallback called");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof MobilisServiceDiscoveryBean) {
				if (b.getType() == XMPPBean.TYPE_RESULT) {
					if (((MobilisServiceDiscoveryBean) b)
							.getDiscoveredServices() != null
							&& !(((MobilisServiceDiscoveryBean) b)
									.getDiscoveredServices()).isEmpty()) {
						currentInstancesCount = ((MobilisServiceDiscoveryBean) b)
								.getDiscoveredServices().size();

						// Query infos for every game
						for (MobilisServiceInfo serviceInfo : ((MobilisServiceDiscoveryBean) b)
								.getDiscoveredServices()) {
							sendGameInformationMessage(serviceInfo.getJid());
							// Game.getInstance().getInstances().put(strings[3],
							// null);
						}
					} else {
						// sendCreateGameMessage();
						currentInstancesCount = 0;
						gamingClient.finishedLoadingInstances();
					}

					beansWaitingForAck.remove(b.getId());

				}
			}

		}
	};

	/**
	 * The GameInformationBean callback. Bean from type result expected.
	 * Contains game infos, see corresponding Bean for details.
	 */
	IXMPPIQCallback gameInformationBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "gameInformationBeanCallback called");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof GameInformationBean) {

				if (b.getType() == XMPPBean.TYPE_RESULT) {
					Log.v(TAG, "Recieved Instance JID: " + b.getFrom());
					Log.v(TAG, "Current Instances:");
					for (Instance instance : Game.getInstance().getInstances()) {
						Log.v(TAG, "Instance JID: " + instance.getInstanceJID());
					}

					Instance instance = null;
					for (Instance tempInstance : Game.getInstance()
							.getInstances()) {
						if (tempInstance.getInstanceJID().equals(b.getFrom())) {
							instance = tempInstance;
							Log.v(TAG, "Instance already in List. ID:"
									+ tempInstance.getInstanceJID());
						}
					}
					if (instance == null) {
						instance = new Instance(b.getFrom());
						Log.v(TAG, "Instance not in List. Create new one. ID:"
								+ instance.getInstanceJID());
						Game.getInstance().getInstances().add(instance);
					}

					instance.setOpener(((GameInformationBean) b)
							.getOpenerName());
					instance.setPlayers(((GameInformationBean) b)
							.getPlayerNames());
					instance.setMaxMemberCount(((GameInformationBean) b)
							.getMaxMemberCount());

					gamingClient.refreshInstances();

					if (Game.getInstance().getInstances().size() >= currentInstancesCount) {
						gamingClient.finishedLoadingInstances();
					}

					// TODO:
					// Replace by GUI Interaction
					// Currently first game is joined automatically.
					// if
					// (!Game.getInstance().getInstances().containsValue(null)
					// && currentInstancesCount > 0){
					// for(Instance _instance_ :
					// Game.getInstance().getInstances().values()){
					// if(!(_instance_.getPlayers().size() >=
					// _instance_.getMaxMemberCount()))sendJoinGameMessage(Game.getInstance().getClientPlayer(),_instance_.getInstanceJID());
					// }
					// }

					beansWaitingForAck.remove(b.getId());

				}
			}

		}
	};

	/**
	 * The CreateNewServiceInstanceBean callback. Bean from type result
	 * expected. Sender JID is XMPP Id for new game instance.
	 */
	IXMPPIQCallback createNewServiceInstanceBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "createNewServiceInstanceBeanCallback called");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof CreateNewServiceInstanceBean) {
				if (b.getType() == XMPPBean.TYPE_RESULT) {
					// Join the created game. (Expected Behavior)
					sendJoinGameMessage(Game.getInstance().getClientPlayer(),
							((CreateNewServiceInstanceBean) b)
									.getJidOfNewService());

					beansWaitingForAck.remove(b.getId());

				}
			}

		}
	};

	/** The JoinGameBean callback. Type Result expected. */
	IXMPPIQCallback joinGameBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "joinGameBeanCallback called");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof JoinGameBean) {
				if (b.getType() == XMPPBean.TYPE_RESULT) {
					Game.getInstance().setConnected(true);
					beansWaitingForAck.remove(b.getId());
					Game.getInstance().setGameID(b.getFrom());

				}
			}

		}
	};

	/**
	 * The PlayerUpdateBean callback. Type Result expected. Contains player
	 * information, see corresponding Bean for details.
	 */
	IXMPPIQCallback playerUpdateBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "playerUpdateBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof PlayerUpdateBean) {
				if (b.getType() == XMPPBean.TYPE_SET) {
					Map<String, Integer> players = ((PlayerUpdateBean) b)
							.getPlayers();
					Map<String, String> names = ((PlayerUpdateBean) b)
							.getNames();
					Map<String, Boolean> states = ((PlayerUpdateBean) b)
							.getStates();
					Map<String, GeoPosition> positions = ((PlayerUpdateBean) b)
							.getPositions();

					for (String playerID : players.keySet()) {
						int teamID = players.get(playerID);
						Player player = Game.getInstance().getPlayers()
								.get(playerID);
						Team team = Game.getInstance().getTeams().get(teamID);

						if (player == null)
							player = new Player(names.get(playerID), playerID);
						if (team == null)
							team = new Team(teamID);

						if (player.getPlayerID().equalsIgnoreCase(
								iXMPPService.getUsername())) {
							Game.getInstance().setClientPlayer(player);
						}

						player.setPlayername(names.get(playerID));
						// Log.v(TAG,player.getPlayername());
						player.setTeam(team);
						// Log.v(TAG,player.getTeam().toString());
						player.setState(states.get(playerID));
						// Log.v(TAG,String.valueOf(player.isState()));
						player.setPosition(positions.get(playerID));
						// Log.v("playerpos from server",
						// positions.get(playerID).toString());
						Game.getInstance().addPlayer(player);
						Game.getInstance().addTeam(team);
					}

					// aussortieren von nichtvorhanden spielern -> concurrency
					LinkedList<Player> deletedList = new LinkedList<Player>();
					for (Player _player : Game.getInstance().getPlayers()
							.values()) {
						if (!players.containsKey(_player.getPlayerID())) {
							deletedList.add(_player);
						}
					}
					for (Player deletePlayer : deletedList)
						Game.getInstance().removePlayer(deletePlayer);

					for (Team _team : Game.getInstance().getTeams().values()) {
						if (_team.getPlayers().size() == 0)
							Game.getInstance().removeTeam(_team);
					}

					// Log.v(TAG,Game.getInstance().toString());

					Game.getInstance().setConnected(true);

					gamingClient.refreshPlayers();
					// gamingClient.refreshGame();
					sendAck(b);
				}
			}

		}
	};

	/**
	 * The StartGameBean callback. Type Set expected. Contains association
	 * between barcodes, geopositions and cards.
	 */
	IXMPPIQCallback startGameBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "startGameBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b.getType() == XMPPBean.TYPE_SET) {
				// Game.getInstance().setGameID(
				// ((StartGameBean) b).getGameID());

				Map<String, GeoPosition> barcodes = ((StartGameBean) b)
						.getBarcodes();
				Map<String, String> pictures = ((StartGameBean) b)
						.getPictures();

				for (String barcode : barcodes.keySet()) {
					Card card = new Card(barcode, pictures.get(barcode),
							barcodes.get(barcode));

					try {

						Pair pair = null;
						for (Pair currPair : Game.getInstance().getPairs()
								.values()) {
							if (currPair.getPairID().equalsIgnoreCase(
									card.getPairID())) {
								pair = currPair;
							}
						}

						if (pair == null) {
							if (card.getPairID().equals("memory01"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory01));
							if (card.getPairID().equals("memory02"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory02));
							if (card.getPairID().equals("memory03"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory03));
							if (card.getPairID().equals("memory04"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory04));
							if (card.getPairID().equals("memory05"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory05));
							if (card.getPairID().equals("memory06"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory06));
							if (card.getPairID().equals("memory07"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory07));
							if (card.getPairID().equals("memory08"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory08));
							if (card.getPairID().equals("memory09"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory09));
							if (card.getPairID().equals("memory10"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory10));
							if (card.getPairID().equals("memory11"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory11));
							if (card.getPairID().equals("memory12"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory12));
							if (card.getPairID().equals("memory13"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory13));
							if (card.getPairID().equals("memory14"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory14));
							if (card.getPairID().equals("memory15"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory15));
							if (card.getPairID().equals("memory16"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory16));
							if (card.getPairID().equals("memory17"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory17));
							if (card.getPairID().equals("memory18"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory18));
							if (card.getPairID().equals("memory19"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory19));
							if (card.getPairID().equals("memory20"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory20));
							if (card.getPairID().equals("memory21"))
								pair = new Pair(card.getPairID(),
										BitmapFactory.decodeResource(
												LocPairsApp.getContext()
														.getResources(),
												R.drawable.memory21));
							Game.getInstance().addPair(pair);
						}
						pair.addCard(card);
						Log.v(TAG, "put card " + card.getBarcode()
								+ " to pair " + pair.getPairID());

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				gamingClient.startGame();
				startGameBean = b;

			}
		}
	};

	/**
	 * Acknowledge startGameBean. When StartGameIQ is acknowledged a
	 * StartRoundIQ follows immediately - this causes a problem when
	 * InRoundPlayingActivity is not started yet. So the StartGameBean is stored
	 * and this Method for acknowledging it is called by InRoundPlayingActivity.
	 */
	public boolean acknowledgeStartGameBean() {
		return sendAck(startGameBean);
	}

	/**
	 * The StartRoundBean callback. Type Set expected. Contains active team and
	 * time frame of next round.
	 */
	IXMPPIQCallback startRoundBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			try {
				if(viewCallBackDebugOutput) Log.v(TAG, "startRoundBeanCallback invoked.");

				XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
				callbackDebugOutput(b);

				if (b.getType() == XMPPBean.TYPE_SET) {
					try {

						if (Game.getInstance().getActiveRound() == null
								|| ((StartRoundBean) b).getRoundNumber() > Game
										.getInstance().getActiveRound().getId()) {

							Log.v(TAG, "ClientTeam: "
									+ Game.getInstance().getClientPlayer()
											.getTeam().toString());
							for (Team team : Game.getInstance().getTeams()
									.values()) {
								Log.v(TAG, team.toString());
							}

							Time startTime = new Time();
							try {
								// Correct startime by removing calculated
								// offset to real world time
								startTime.set(((StartRoundBean) b)
										.getStartTimeInMilliseconds()
										- timeOffset);
							} catch (ParseException e) {
								e.printStackTrace();
							}

							Time stopTime = new Time();
							stopTime.set(startTime.toMillis(false)
									+ ((StartRoundBean) b).getDuration());

							Round nextRound = new Round(
									((StartRoundBean) b).getRoundNumber(),
									((StartRoundBean) b).getActive(),
									startTime, stopTime);
							Game.getInstance().setNextRound(nextRound);

							for (Integer teamID : ((StartRoundBean) b)
									.getTeamScores().keySet()) {
								Game.getInstance()
										.getPoints()
										.put(teamID,
												((StartRoundBean) b)
														.getTeamScores().get(
																teamID));
							}

							for (Card card : Game.getInstance().getCards()
									.values()) {
								card.coverCard();
							}

							Game.getInstance()
									.setActiveTeam(
											Game.getInstance()
													.getTeams()
													.get(Integer
															.parseInt(((StartRoundBean) b)
																	.getActiveTeam())));

							Log.v(TAG, nextRound.toString());
							gamingClient.initializeNextRound();

							sendAck(b);
						}
					} catch (NullPointerException npe) {
						Log.e(TAG,
								"StartRoundBean caused NullPointerException: ");
						npe.printStackTrace();
					}

				}
			} catch (Exception e) {
				Log.e(TAG, "Exception in StartRoundBeanCallback");
				e.printStackTrace();
			}
		}
	};

	/** The GoThereBean callback. Type Set Expected. Contains position to go to. */
	IXMPPIQCallback goThereBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "goThereBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof GoThereBean) {
				if (b.getType() == XMPPBean.TYPE_SET) {

					Game.getInstance().setGoThere(
							((GoThereBean) b).getPosition());
					Log.v("goThere", "gothere "
							+ Game.getInstance().getGoThere());
					sendAck(b);
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					gamingClient.refreshGame();

				}
			}
		}
	};

	/** The QuitBeanCallback. Type Result expected. Contains reason. */
	IXMPPIQCallback quitBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "quitBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof QuitBean) {

				if (b.getType() == XMPPBean.TYPE_SET) {
					sendAck(b);
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					Game.getInstance().setConnected(false);
					beansWaitingForAck.remove(b.getId());

					Log.v(TAG, "Following Beans have not been confirmed: ");
					for (XMPPBean _b : beansWaitingForAck.values()) {
						Log.v(TAG, _b.getId() + " " + _b.getClass());
					}

					gamingClient.quitGame();

				}
			}
		}
	};

	/**
	 * The UncoverCardBean callback. Type Result expected. Only for confirmation
	 * purposes.
	 */
	IXMPPIQCallback uncoverCardBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "uncoverCardCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b.getType() == XMPPBean.TYPE_RESULT) {
				beansWaitingForAck.remove(b.getId());
			}
		}
	};

	/**
	 * The EndGameBean callback. Type Set expected. Contains all-time Highscore
	 * and Points.
	 */
	IXMPPIQCallback endGameBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "endGameBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof EndGameBean) {

				if (b.getType() == XMPPBean.TYPE_SET) {
					sendAck(b);

					for (int teamId : ((EndGameBean) b).getPoints().keySet()) {
						Game.getInstance()
								.getPoints()
								.put(teamId,
										((EndGameBean) b).getPoints().get(
												teamId));
						Log.v(TAG, "Points add: Team " + teamId + "   Points " +  ((EndGameBean) b).getPoints().get(
								teamId));
					}

					Game.getInstance().setHighscore(
							((EndGameBean) b).getHighscores());

					Log.v(TAG, "Following Beans have not been confirmed: ");
					for (XMPPBean _b : beansWaitingForAck.values()) {
						Log.v(TAG, _b.getId() + " " + _b.getClass());
					}

					gamingClient.quitGame();

				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					beansWaitingForAck.remove(b.getId());
				}
			}
		}
	};

	/**
	 * The KeepAliveBean callback. Type result expected. Only for confirmation
	 * purposes.
	 */
	IXMPPIQCallback keepAliveBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "keepAliveBeanCallback invoked.");

			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			callbackDebugOutput(b);

			if (b instanceof KeepAliveBean) {

				if (b.getType() == XMPPBean.TYPE_RESULT) {
					beansWaitingForAck.remove(b.getId());
				}
			}
		}
	};

	/**
	 * The ShowCardBean callback. Type Set expected. Tells the client to show a
	 * specific card.
	 */
	IXMPPIQCallback showCardBeanCallback = new IXMPPIQCallback.Stub() {
		public void processIQ(XMPPIQ iq) throws RemoteException {
			if(viewCallBackDebugOutput) Log.v(TAG, "showCardBeanCallback invoked.");

			try {
				XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
				callbackDebugOutput(b);

				if (b instanceof ShowCardBean) {

					if (b.getType() == XMPPBean.TYPE_SET) {
						Game.getInstance().getCards()
								.get(((ShowCardBean) b).getBarCodeId())
								.uncoverCard();
						gamingClient.refreshGame();
						sendAck(b);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	// ------------------- Android Service Stuff ------------------- //

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	public void onCreate() {
		started = System.currentTimeMillis();
		startService();

		timerHandler.removeCallbacks(timerRunnableReconnect);
		timerRunnableReconnect.run();
	}

	/** The on service. */
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			gamingClient = ((GamingClient.LocalBinder) rawBinder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			gamingClient = null;
		}
	};

	/**
	 * The Class LocalBinder.
	 */
	public class LocalBinder extends Binder {

		/**
		 * Gets the service.
		 * 
		 * @return the service
		 */
		public LocPairsController getService() {
			return (LocPairsController.this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return (binder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		disconnect();
		super.onDestroy();
	}

	/**
	 * Start service.
	 */
	public void startService() {

	}

	/**
	 * Gets the application context.
	 * 
	 * @return the context
	 */
	public Context getContext() {
		return ctx;
	}

	/**
	 * Calculates and sets the time offset. Uses NTP.
	 */
	private void setTimeOffset() {
		SntpClient client = new SntpClient();
		if (client.requestTime("0.de.pool.ntp.org", 5000)) {
			// long now = client.getNtpTime() + System.elapsedRealtime() -
			// client.getNtpTimeReference();

			Time systemTime = new Time();
			systemTime.set(System.currentTimeMillis());
			Log.v(TAG, "System Time: " + systemTime);

			Time ntpTime = new Time();
			ntpTime.set(client.getNtpTime());
			Log.v(TAG, "NTP Time: " + ntpTime);

			timeOffset = client.getNtpTime() - System.currentTimeMillis();
			Log.v(TAG, "Offset: " + String.valueOf(timeOffset));

			Time nowTime = new Time();
			nowTime.set(System.currentTimeMillis() + timeOffset);
			Log.v(TAG, "Corrected Time: " + nowTime);
		}
	}

	/**
	 * Checks if device has internet connection.
	 * 
	 * @return true, if is online
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * connect to wfs server (building data).
	 */
	public void connectWFS() {
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				WFSConnector wfsConnector = new WFSConnector();
				wfsConnector.connectToServer();
				Game.getInstance().setModelDownloaded(true);
				Looper.loop();
			}
		}).start();

	}

}
