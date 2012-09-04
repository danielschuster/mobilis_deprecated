package de.tudresden.inf.rn.mobilis.groups;


interface IGroupManager {
	void connectToMXA();
	void connectToXMPPServer();
	void sendGroupInfoBeanGet(String groupId);
	void sendGroupDeleteBeanSet(String groupId);
	void sendGroupLeaveBeanSet(String groupId);
	void sendGroupMemberInfoBeanGet(String jid);
	void sendGroupJoinBeanSet(String groupId, int userLongitude, int userLatitude);
	void sendGroupMemberInfoBeanSet(String packetId);
	void sendGroupInviteBeanSet(in List<String> invitees, String groupId);
}