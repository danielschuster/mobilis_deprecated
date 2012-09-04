// IFacebookService.aidl
// describes publicly available methods provided by Service FacebookService

package edu.bu.CS893;

interface IFacebookService {
	long getUidFromName(String theName);
	String getPicFromName(String theName);
	String getStatusFromName(String theName);
	String getNameFromUid(long theUid);
	String getPicFromUid(long theUid);
	String getStatusFromUid(long theUid);
}    
