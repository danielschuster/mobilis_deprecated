
package de.tud.android.locpairs.model;

/**
 * The Class Settings.
 */
public class Settings {
	
	/** The instance. */
	private static Settings instance;
	
	/**
	 * Server: 141.30.203.90 : 5222
	 * Ressource: MXA
	 * 
	 * Accounts:
	 * 
	 * server: 7Dj3S
	 * alpha : PZlea
	 * beta  : Pslqt
	 * gamma : pYeYW
	 * delta : fcgZe
	 *	
	 */
	static public String sharedKey = "locpairs2011";
	
	/** The xmpp username. */
	private String xmppUsername = "alpha";
	
	/** The xmpp resource. */
	private String xmppResource = "MXA";
	
	/** The xmpp password. */
	private String xmppPassword = "";
	
	/** The xmpp server. */
	private String xmppServer = "141.30.203.90";
	
	/** The xmpp port. */
	private Integer xmppPort = 5222;
	
	/** The xmpp address. */
	private String xmppAddress = xmppUsername + xmppServer + "/" + xmppResource;
	
	/** The locpairs resource. */
	private String locpairsResource = "Smack";
	
	/** The locpairs address. */
	private String locpairsAddress = "server@" + xmppServer + "/" + locpairsResource;
	
	/** The mobilis resource. */
	private String mobilisResource = "Coordinator";
	
	/** The mobilis server adress. */
	private String mobilisAddress = "mobilis@" + xmppServer + "/" + mobilisResource;
	
	/** The MobilisLocPairsNamespace. */
	private String locPairsNamespace = "http://mobilis.inf.tu-dresden.de#services/LocPairs";
	
	/** The player. */
	private Player player = new Player("Alpha","alphaID");
	
	/** The debug. */
	private boolean debug = true;

	/**
	 * Instantiates a new settings.
	 */
	private Settings(){
		
	};
	
	/**
	 * Gets the single instance of Settings.
	 *
	 * @return single instance of Settings
	 */
	public static Settings getInstance(){
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	/**
	 * Gets the xmpp username.
	 *
	 * @return the xmpp username
	 */
	public String getXmppUsername() {
		return xmppUsername;
	}

	/**
	 * Sets the xmpp username.
	 *
	 * @param xmppUsername the new xmpp username
	 */
	public void setXmppUsername(String xmppUsername) {
		this.xmppUsername = xmppUsername;
	}

	/**
	 * Gets the xmpp resource.
	 *
	 * @return the xmpp resource
	 */
	public String getXmppResource() {
		return xmppResource;
	}

	/**
	 * Sets the xmpp resource.
	 *
	 * @param xmppResource the new xmpp resource
	 */
	public void setXmppResource(String xmppResource) {
		this.xmppResource = xmppResource;
	}

	/**
	 * Gets the xmpp password.
	 *
	 * @return the xmpp password
	 */
	public String getXmppPassword() {
		return xmppPassword;
	}

	/**
	 * Sets the xmpp password.
	 *
	 * @param xmppPassword the new xmpp password
	 */
	public void setXmppPassword(String xmppPassword) {
		this.xmppPassword = xmppPassword;
	}

	/**
	 * Gets the xmpp server.
	 *
	 * @return the xmpp server
	 */
	public String getXmppServer() {
		return xmppServer;
	}

	/**
	 * Sets the xmpp server.
	 *
	 * @param xmppServer the new xmpp server
	 */
	public void setXmppServer(String xmppServer) {
		this.xmppServer = xmppServer;
	}

	/**
	 * Gets the xmpp port.
	 *
	 * @return the xmpp port
	 */
	public Integer getXmppPort() {
		return xmppPort;
	}

	/**
	 * Sets the xmpp port.
	 *
	 * @param xmppPort the new xmpp port
	 */
	public void setXmppPort(Integer xmppPort) {
		this.xmppPort = xmppPort;
	}

	/**
	 * Gets the xmpp address.
	 *
	 * @return the xmpp address
	 */
	public String getXmppAddress() {
		return xmppAddress;
	}

	/**
	 * Sets the xmpp address.
	 *
	 * @param xmppAddress the new xmpp address
	 */
	public void setXmppAddress(String xmppAddress) {
		this.xmppAddress = xmppAddress;
	}

	/**
	 * Gets the locpairs resource.
	 *
	 * @return the locpairs resource
	 */
	public String getLocpairsResource() {
		return locpairsResource;
	}

	/**
	 * Sets the locpairs resource.
	 *
	 * @param locpairsResource the new locpairs resource
	 */
	public void setLocpairsResource(String locpairsResource) {
		this.locpairsResource = locpairsResource;
	}

	/**
	 * Sets the locpairs ressource.
	 *
	 * @param locpairsRessource the new locpairs ressource
	 */
	public void setLocpairsRessource(String locpairsRessource) {
		this.locpairsResource = locpairsRessource;
	}

	/**
	 * Gets the locpairs address.
	 *
	 * @return the locpairs address
	 */
	public String getLocpairsAddress() {
		return locpairsAddress;
	}

	/**
	 * Sets the locpairs address.
	 *
	 * @param locpairsAddress the new locpairs address
	 */
	public void setLocpairsAddress(String locpairsAddress) {
		this.locpairsAddress = locpairsAddress;
	}

	/**
	 * Gets the player.
	 *
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets the player.
	 *
	 * @param player the new player
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	/**
	 * Checks if is debug.
	 *
	 * @return true, if is debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Gets the mobilis resource.
	 *
	 * @return the mobilis resource
	 */
	public String getMobilisResource() {
		return mobilisResource;
	}

	/**
	 * Sets the mobilis resource.
	 *
	 * @param mobilisResource the new mobilis resource
	 */
	public void setMobilisResource(String mobilisResource) {
		this.mobilisResource = mobilisResource;
	}

	/**
	 * Gets the mobilis address.
	 *
	 * @return the mobilis address
	 */
	public String getMobilisAddress() {
		return mobilisAddress;
	}

	/**
	 * Sets the mobilis address.
	 *
	 * @param mobilisAddress the new mobilis address
	 */
	public void setMobilisAddress(String mobilisAddress) {
		this.mobilisAddress = mobilisAddress;
	}

	/**
	 * Gets the loc pairs namespace.
	 *
	 * @return the loc pairs namespace
	 */
	public String getLocPairsNamespace() {
		return locPairsNamespace;
	}

	/**
	 * Sets the loc pairs namespace.
	 *
	 * @param locPairsNamespace the new loc pairs namespace
	 */
	public void setLocPairsNamespace(String locPairsNamespace) {
		this.locPairsNamespace = locPairsNamespace;
	}

}
