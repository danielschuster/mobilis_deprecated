package de.tudresden.inf.rn.mobilis.android.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;

import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Credential;

/**
 * Class representing a database session (transaction). Singleton allows only one session at a time.
 * @author Dirk
 */
public class DBHelper {

    private static DBHelper dbSession = null;
    private ObjectContainer db;
    private Context androidContext;
    
    /**
     * Starts a new DB session by opening the DB file.
     */
    private DBHelper() {
        SessionService s = SessionService.getInstance();
        androidContext = s.getContext();
        String dbFile = s.getPreferences().getString("pref_db_file", "AndroidBuddy.db4o");
        db = Db4o.openFile(getDbConfig(), getDbPath(dbFile));
    }
        
    /**
     * Opens the DB, if not already done.
     * @return DB the current DB session
     */
    public static DBHelper getDB() {
        if (dbSession == null) {
            dbSession = new DBHelper();
        }
        return dbSession;
    }
        
    private static Configuration getDbConfig(){
        Configuration c = Db4o.newConfiguration();
        c.lockDatabaseFile(false);
        return c;
    } 
    
    private String getDbPath(String localPath) {
        return androidContext.getFilesDir() + "/" + localPath;
    }

    /**
     * Performs an explicit commit for the current session to make all stored changes persistent.
     */
    public void commit() {
        db.commit();
    }
    
    /**
     * Closes the DB session and therewith the DB file.
     */
    public void close() {
        db.close();
        dbSession = null;
    }
    
    public void store(Object object) {
        db.store(object);
    }
    
    public void delete(Object object) {
        db.delete(object);
    }
    
    /**
     * Fetches the account credential (which is found first) for a given user-ID and a networkName.
     * @param userId the given user-ID
     * @param networkName the given network name
     * @return Credential
     */
    public Credential getCredential(final String userId, final String networkName) {
        List<Credential> credentials = db.query(new Predicate<Credential>(){
            private static final long serialVersionUID = 1L;
            @Override
            public boolean match(Credential c) {
                if (c.getUserId().equals(userId) && c.getNetworkName().equals(networkName)) {
                    return true;
                }
                else return false;
            }
        });
        if (credentials.size() > 0) return credentials.get(0);
        else return null;
    }
    
    /**
     * Fetches all stored credentials from the db.
     * @return List<Credential>
     */
    public List<Credential> getAllCredentials() {
        return db.query(Credential.class);
    }
    
    /**
     * Fetches all stored credentials from the db, which should be considered for autoconnect.
     * @param network the network name to restrict the search for, or null for all networks
     * @return List<Credetial>
     */
    public List<Credential> getAutoConCredentials(final String network) {
        List<Credential> credentials = db.query(new Predicate<Credential>(){
            private static final long serialVersionUID = 1L;
            @Override
            public boolean match(Credential c) {
                if (network != null) {
                    if (c.getNetworkName().equals(network)) return c.isAutoConnect();
                    else return false;
                } else return c.isAutoConnect();
            }
        });
        return credentials;
    }
    
    public <T> ArrayList<T> getArrayList(List<T> list) {
        return new ArrayList<T>(list);
    }
}
