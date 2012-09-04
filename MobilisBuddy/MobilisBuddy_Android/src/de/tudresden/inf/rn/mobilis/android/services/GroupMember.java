package de.tudresden.inf.rn.mobilis.android.services;

import java.util.Collections;
import java.util.HashSet;

public class GroupMember {

	private String mJid;
	private HashSet<String> mGroups;
	private String mPresenceMode;

	/**
	 * Constructor of a GroupMember, the jabber id has to be specified.
	 * 
	 * @param jid
	 *            Full jabber id, including resource.
	 */
	public GroupMember(String jid) {
		mGroups = new HashSet<String>();
		mJid = jid;
	}

	public String getJid() {
		return mJid;
	}

	public void setJid(String jid) {
		mJid = jid;
	}

	public String getPresenceMode() {
		return mPresenceMode;
	}

	public void setPresenceMode(String presenceMode) {
		mPresenceMode = presenceMode;
	}

	public HashSet<String> getGroups() {
		return (HashSet<String>) Collections.unmodifiableSet(mGroups);
	}
	
	public boolean isMemberOf(String group) {
		return mGroups.contains(group);
	}
	
	public void addMembership(String group) {
		mGroups.add(group);
	}
	
	public void removeMembership(String group) {
		if (mGroups.contains(group))
			mGroups.remove(group);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GroupMember) {
			return (mJid.equals(((GroupMember) o).getJid()));
		}
		return false;
	}
}
