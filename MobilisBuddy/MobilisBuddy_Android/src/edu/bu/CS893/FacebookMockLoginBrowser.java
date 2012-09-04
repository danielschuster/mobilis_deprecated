package edu.bu.CS893;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import edu.bu.CS893.data.FacebookLoginResponse;

import android.util.Log;

public class FacebookMockLoginBrowser {
	private static String LOGIN_POST_URL = "https://login.facebook.com/login.php";
	private static String TOS_POST_URL = "https://www.facebook.com/tos.php?v=1.0";
	
	public static boolean login(String theApiKey, String theAuthKey, String theLogin, String thePassword) {

		// set a dummy HTTPS hostname verifier which lets anything pass
		
		HttpsURLConnection.setDefaultHostnameVerifier(
			new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			}
		);
		
		FacebookLoginResponse aLoginResponse = doLogin(theApiKey, theAuthKey, theLogin, thePassword);
		boolean success = false;
		if (aLoginResponse != null) {
		    String path = aLoginResponse.getLocation().getPath();
		    if ((path.equalsIgnoreCase("/desktopapp.php")) || 
		            (path.equalsIgnoreCase("/tos.php"))) {
		        doConfirmTOS(theApiKey, theAuthKey, aLoginResponse.getCookieString());
		        return true;
		    }
		}
		return success;
	}

	private static FacebookLoginResponse doLogin(String theApiKey, String theAuthKey, String theLogin, String thePassword) {
		try {
			URL aLoginUrl = new URL(LOGIN_POST_URL);

			URLConnection aConnection = aLoginUrl.openConnection();
			
			// this is the cookie that facebook sets to test if we are a browser with cookies enabled
			aConnection.setRequestProperty("Cookie", "test_cookie=1");
			
			// prepare for form data
			aConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			aConnection.setDoOutput(true);
			
			DataOutputStream aOutStream = new DataOutputStream(aConnection.getOutputStream()); // takes long.
			aOutStream.writeBytes(String.format("version=1.0&api_key=%s&auth_token=%s&email=%s&pass=%s&login=login", theApiKey, theAuthKey, theLogin, thePassword));
			
			Map<String, List<String>> aHeaderFields = aConnection.getHeaderFields();
			
			URL aRedirectLocation = null;
			List<String> aRedirectLocations = aHeaderFields.get("location");
			if (aRedirectLocations != null)
				aRedirectLocation = new URL(aRedirectLocations.get(0));
			
			HashMap<String, String> aCookies = null;
			List<String> aCookieResponses = aHeaderFields.get("set-cookie");
			if (aCookieResponses != null) {
				aCookies = new HashMap<String, String>(aCookieResponses.size());
				Pattern aCookiePattern = Pattern.compile("^(.+?)=(.+?);.*"); 
				
				for (String aCookieResponse : aCookieResponses) {
					Matcher aCookieMatcher = aCookiePattern.matcher(aCookieResponse);
					if (aCookieMatcher.matches()) {
						String aKey = aCookieMatcher.group(1);
						String aValue = aCookieMatcher.group(2);
						aCookies.put(aKey, aValue);
					}
				}
			}
			
			if (aCookies != null && aRedirectLocation != null) {
				return new FacebookLoginResponse(aRedirectLocation, aCookies);
			}
		}
		catch (IOException e) {
			Log.v("http", e.toString());
		}	
		
		return null;
	}

	private static URL doConfirmTOS(String theApiKey, String theAuthKey, String theCookieString) {
		Log.v("http", theCookieString);
		try {
			URL aTOSUrl = new URL(TOS_POST_URL);
			URLConnection aConnection = aTOSUrl.openConnection();
		
			// set all the cookies that facebook gave us
			aConnection.setRequestProperty("Cookie", theCookieString);
			
			// prepare for form data
			aConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			aConnection.setDoOutput(true);
			
 			DataOutputStream aOutStream = new DataOutputStream(aConnection.getOutputStream()); // takes long.
			aOutStream.writeBytes(String.format("grant_perm=1.0&api_key=%s&auth_token=%s&access_checkbox=on", theApiKey, theAuthKey));
			
			Map<String, List<String>> aHeaderFields = aConnection.getHeaderFields();
			
			URL aRedirectLocation = null;
			List<String> aRedirectLocations = aHeaderFields.get("location");
			if (aRedirectLocations != null)
				aRedirectLocation = new URL(aRedirectLocations.get(0));
			
			return aRedirectLocation;
		}
		catch (IOException e) {
			Log.e("http", e.toString());
		}
		
		return null;
	}

}
