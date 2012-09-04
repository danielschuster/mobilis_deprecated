package edu.bu.CS893.data;

import java.net.URL;
import java.util.HashMap;

public class FacebookLoginResponse {
	private URL _location;
	private HashMap<String, String> _cookies;
	
	public FacebookLoginResponse(URL theLocation, HashMap<String, String> theCookies) {
		_location = theLocation;
		_cookies = theCookies;
	}

	public URL getLocation() {
		return _location;
	}

	public HashMap<String,String> getCookies() {
		return _cookies;
	}
	
	public String getCookieString() {
		if (_cookies == null) return null;
		StringBuilder aBuilder = new StringBuilder();
		for (String aKey : _cookies.keySet()) {
			String aValue = _cookies.get(aKey);
			aBuilder.append(aKey + "=" + aValue + "; "); 
		}
		
		return aBuilder.toString();
	}

}
