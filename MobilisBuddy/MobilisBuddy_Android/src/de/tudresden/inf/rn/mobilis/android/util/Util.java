package de.tudresden.inf.rn.mobilis.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

/**
 * Useful static functions to access from everywhere :)
 * @author Dirk
 */
public class Util {

    /**
     * Returns the displayed name of the given internal network constant.
     * @see Const
     * @param networkName the internal network name constant
     * @return the displayed name of the network
     */
    public static String getDisplayedNetworkName(String networkName) {
        Context ctx = SessionService.getInstance().getContext();
        String displayedNetworkName = networkName;
        if (networkName.toLowerCase().equals(Const.LOCALDB)) 
            displayedNetworkName = ctx.getResources().getString(R.string.network_localdb_name);
        if (networkName.toLowerCase().equals(Const.MOBILIS))
            displayedNetworkName = ctx.getResources().getString(R.string.network_mb_name);
        if (networkName.toLowerCase().equals(Const.FACEBOOK))
            displayedNetworkName = ctx.getResources().getString(R.string.network_fb_name);
        return displayedNetworkName;
    }
    
    /**
     * Shows a simple alert dialog with an okay button. 
     * @param context the context (Activity), which should manage the dialog
     * @param title the dialog title
     * @param message the message to display in the dialog
     */
    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Returns a JabberID string, built from a user name, service name (computer name) and
     * a resource name (i.e. Android). All strings will be set to lowercase and 
     * blank spaces will be removed. 
     * @param name the user name, 
     * @param service the service name
     * @param resource the resource name
     * @return String - the built JabberID from the given params
     */
    public static String getJabberID(String name, String service, String resource) {
        if ((name == null) || (service == null)) return null;
        String tempName = name.replaceAll(" ", "").toLowerCase();
        String tempService = service.replaceAll(" ", "").toLowerCase();
        if (resource != null) {
            String tempRes = service.replaceAll(" ", "").toLowerCase();
            return tempName + "@" + tempService + "/" + tempRes;
        } else {
            return tempName + "@" + tempService;
        }
    }
    
    /**
     * Returns only the name part of the jabber ID.
     * @param fullJabberID
     * @return String
     */
    public static String getNameFromJabberID(String fullJabberID) {
        if (fullJabberID == null) return "";
    	int index = fullJabberID.indexOf('@');
        if (index < 0) return fullJabberID;
        if (index == 0) return "";
        else {
            return fullJabberID.substring(0, index);
        }
    }
}
