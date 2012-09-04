/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: wfs
 */
package de.tud.iiogis.wfs;


//import de.tud.iiogis.IIOGISApp;
import java.util.ArrayList;
import de.tud.server.model.LocationModelAPI;


/**
 * Class: WFSAPI.java
 * Function: control communication with and manage wfs servers
 *
 * @author Jan Scholze
 * @version 1.18
 */
public class WFSAPI {

    /**
     * start task to send GetCapabilities request (get server name and provided layers)
     *
     * @param WFSServer server - wfs server to communicate with
     * @return WFSGetCapabilitiesTask - getcapabilities task (needed by the main application to manage the status icon)
     */
//    public static WFSGetCapabilitiesTask getCapabilities(WFSServer server){
//        WFSGetCapabilitiesTask getCapabilitiesTask = new WFSGetCapabilitiesTask(org.jdesktop.application.Application.getInstance(IIOGISApp.class), server);
//        ApplicationContext appC = Application.getInstance().getContext();
//        TaskMonitor tM = appC.getTaskMonitor();
//        TaskService tS = appC.getTaskService();
//        tS.execute(getCapabilitiesTask);
//        tM.setAutoUpdateForegroundTask(true);
//        tM.setForegroundTask(getCapabilitiesTask);
//        return getCapabilitiesTask;
//    }

    /**
     * start task to send DescribeFeatureType request (get properties of each server layer)
     *
     * @param WFSServer server - wfs server to communicate with
     * @return WFSDescribeFeatureTypeTask - decribefeaturetype task (needed by the main application to manage the status icon)
     */
//    public static WFSDescribeFeatureTypeTask describeFeatureType(WFSServer server){
//        WFSDescribeFeatureTypeTask describeFeatureTypeTask = new WFSDescribeFeatureTypeTask(org.jdesktop.application.Application.getInstance(IIOGISApp.class), server);
//        ApplicationContext appC = Application.getInstance().getContext();
//        TaskMonitor tM = appC.getTaskMonitor();
//        TaskService tS = appC.getTaskService();
//        tS.execute(describeFeatureTypeTask);
//        tM.setAutoUpdateForegroundTask(true);
//        tM.setForegroundTask(describeFeatureTypeTask);
//        return describeFeatureTypeTask;
//    }

    /**
     * start task to send GetFeature request (get data of each server layer and integrate into location model)
     *
     * @param WFSServer server - wfs server to communicate with
     * @return WFSGetFeatureTask - getfeature task (needed by the main application to manage the status icon)
     */
//    public static WFSGetFeatureTask getFeature(WFSServer server){
//        WFSGetFeatureTask getFeatureTask = new WFSGetFeatureTask(org.jdesktop.application.Application.getInstance(IIOGISApp.class), server);
//        ApplicationContext appC = Application.getInstance().getContext();
//        TaskMonitor tM = appC.getTaskMonitor();
//        TaskService tS = appC.getTaskService();
//        tS.execute(getFeatureTask);
//        tM.setAutoUpdateForegroundTask(true);
//        tM.setForegroundTask(getFeatureTask);
//        return getFeatureTask;
//    }

    /**
     * create new wfs server object
     *
     * @param String name - name of the wfs server
     * @param String url - url of the wfs server
     * @param String title - title of the wfs server
     * @return WFSServer - created wfs server
     */
    public static WFSServer createWFSServer(String name, String url, String title){
        return new WFSServer(name, url, title);
    }

    /**
     * check if server with given url is already existing
     *
     * @param String url - url of the wfs server to check
     * @return boolean - true: a server with the given url is already existing, false: a server with the given url is not existing yet
     */
    public static boolean isWFSServerAlreadyExisting(String url){
        ArrayList<WFSServer> availableServers = LocationModelAPI.getAvailableWFSServers();
        ArrayList<WFSServer> connectedServers = LocationModelAPI.getConnectedWFSServers();
        for(int i = 0; i < availableServers.size(); i++){
            if(availableServers.get(i).getWfsUrl().equals(url)){
                System.out.println("A WFS-Server with the URL \"" + url + "\" is already existing!");
                //JOptionPane.showMessageDialog(null, "A WFS-Server with this URL is already existing!");
                return true;
            }
        }
        for(int i = 0; i < connectedServers.size(); i++){
            if(connectedServers.get(i).getWfsUrl().equals(url)){
                System.out.println("A WFS-Server with the URL \"" + url + "\" is already existing!");
                //JOptionPane.showMessageDialog(null, "A WFS-Server with this URL is already existing!");
                return true;
            }
        }
        return false;
    }


    /**
     * check if the given server url is valid
     *
     * @param String url - url of the wfs server to check
     * @return boolean - true: the given server url seems to be valid, false: the given server url seems not to be valid
     */
    public static boolean isWFSServerURLValid(String url){
        url = url.toLowerCase();
        if (!url.startsWith("http://")){
            //System.out.println("WFS-Server URL not starting with \"http://\"!");
            return false;
        }
        if (!url.contains(".map&")){
            //System.out.println("WFS-Server URL does not refer to a map-file!");
            return false;
        }
        if (!url.contains("&service=wfs")){
            //System.out.println("WFS-Server URL does not specify that the service is \"WFS\"!");
            return false;
        }
        if (!url.contains("&version=")){
            //System.out.println("WFS-Server URL does not specify a version of the WFS-Service!");
            return false;
        }
        return true;
    }

}
