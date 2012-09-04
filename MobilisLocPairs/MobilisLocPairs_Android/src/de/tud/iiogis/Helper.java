/*
 * IIOGIS - Integrated Indoor and Outdoor Geographic Information System
 * Package: main
 */
package de.tud.iiogis;


import de.javagis.jgis.geometry.Point;
import de.javagis.jgis.geometry.PointFactory;
import de.tud.server.model.Coordinate;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.DataFormatException;
//import javax.swing.JOptionPane;
//import org.jdesktop.swingx.mapviewer.GeoPosition;


/**
 * Class: Helper.java
 * Function: several methods for parsing and converting etc.
 *
 * @author Jan Scholze
 * @version 1.25
 */
public class Helper {

    // PARSER

        /**
         * parse coordinates and return as arraylist of points (de.javagis.jgis.geometry.Point)
         *
         * @param String coord - string with coordinates
         * @param String regex - seperator between two coordinate strings
         * @param boolean polygon - if true, the last coordinate will be ignored (in polygones first = last coordinate)
         * @return ArrayList<Point> - arraylist of point-objects
         */
        public static ArrayList<Point> parseCoordinates(String coord, String regex, boolean polygon){
            int end = 0;
            if (polygon) end = 1;
            ArrayList<Point> points = new ArrayList<Point>();
            String[] coordinates = coord.split(regex);
            for (int i = 0; i < (coordinates.length - end); i++){
                // longitude -> x, latitude -> y
                points.add(parseCoordinate(coordinates[i], ","));
            }
            return points;
        }

        /**
         * parse coordinates and return as single point (de.javagis.jgis.geometry.Point)
         *
         * @param String coord - String with coordinates
         * @param String regex - seperator between the coordinate values
         * @return Point - point-object of the coordinates
         */
        public static Point parseCoordinate(String coord, String regex){
            return PointFactory.createPoint(Double.parseDouble(coord.split(regex)[0]), Double.parseDouble(coord.split(regex)[1]));
        }

//        /**
//         * parse textfield-strings to get latitude and longitude
//         *
//         * @param GeoPosition mapCenter - current center position of map
//         * @param String latitudeTextField - string with latitude input
//         * @param String longitudeTextField - string with longitude input
//         * @return GeoPosition - geoposition-object with given latitude and longitude
//         */
//        public static GeoPosition getGeoPosition(GeoPosition mapCenter, String latitudeTextField, String longitudeTextField){
//            // parse input-strings
//            double latitude = parseLatitude(latitudeTextField);
//            double longitude = parseLongitude(longitudeTextField);
//            
//            // set new position if value are in range
//            if (((latitude >= -90) && (latitude <= 90)) && ((longitude >= -180) && (longitude <= 180))){
//                //System.out.println("CURRENT POSITION: Latitude: " + latitude + ", Longitude: " + longitude);
//                return new GeoPosition(latitude, longitude);
//            }else{
//                //  return saved position of current map center
//                return mapCenter;
//            }
//        }

        /**
         * get latitude format
         *
         * @param String inputLat - string with latitude
         * @return boolean - true: coordinates in DMS (Degrees, Minutes, Seconds); false: coordinates in DD (Decimal Degrees)
         */
        public static boolean getLatitudeFormat(String inputLat){
            String[] coordLat;

            // split input-string
            coordLat = inputLat.split(" ");

            if (coordLat.length == 4){
                // prompted coordinates in DMS (Degrees, Minutes, Seconds)
                return true;
            }else{
                // prompted coordinates in DD (Decimal Degrees)
                return false;
            }
        }

        /**
         * get longitude format
         *
         * @param String inputLong - string with longitude
         * @return boolean - true: coordinates in DMS (Degrees, Minutes, Seconds); false: coordinates in DD (Decimal Degrees)
         */
        public static boolean getLongitudeFormat(String inputLong){
            String[] coordLong;

            // split input-string
            coordLong = inputLong.split(" ");

            if (coordLong.length == 4){
                // prompted coordinates in DMS (Degrees, Minutes, Seconds)
                return true;
            }else{
                // prompted coordinates in DD (Decimal Degrees)
                return false;
            }
        }

//        /**
//         * parse latitude and return as double value
//         *
//         * @param String inputLat - string with latitude
//         * @return double - double value of latitude string
//         */
//        public static double parseLatitude(String inputLat){
//            // declarations
//            double latitude = 100;
//            int degreesLat, minutesLat;
//            double secondsLat;
//            String nsLat;
//            String[] coordLat;           
//
//            // split input-string
//            coordLat = inputLat.split(" ");
//
//            if (coordLat.length != 4){
//                // prompted coordinates in DD (Decimal Degrees)
//
//                //System.out.print("(DD) ");
//                try{
//                    // parse decimal degrees
//                    latitude = Double.parseDouble(inputLat);
//                    // check ranges
//                    if ((latitude < -90) || (latitude > 90)) throw new DataFormatException();
//
//                }catch(NumberFormatException e){
//                    System.out.println("ERROR: could not parse latitude");
//                    JOptionPane.showMessageDialog(null, "Please validate the prompted latitude (DMS or DD)!");
//                }catch (DataFormatException e){
//                    System.out.println("ERROR: latitude out of range");
//                    JOptionPane.showMessageDialog(null, "Latitude out of range\n(-90 -> 90)!");
//                }
//            }else{
//                if (coordLat.length == 4){
//                    // prompted coordinates in DMS (Degrees, Minutes, Seconds)
//
//                    //System.out.print("(DMS) ");
//                    try{
//                        // parse degrees, minutes, seconds
//                        degreesLat = Integer.parseInt(((coordLat[0]).split("°"))[0]);
//                        minutesLat = Integer.parseInt(((coordLat[1]).split("'"))[0]);
//                        secondsLat = Double.parseDouble(((coordLat[2]).split("\""))[0]);
//                        nsLat = (coordLat[3]).toString();
//
//                        // check ranges
//                        if ((degreesLat < 0) || (degreesLat > 90)) throw new DataFormatException();
//                        if ((minutesLat < 0) || (minutesLat >= 60)) throw new DataFormatException();
//                        if ((secondsLat < 0) || (secondsLat >= 60)) throw new DataFormatException();
//                        if ((degreesLat == 90) && ((minutesLat > 0) || (secondsLat > 0))) throw new DataFormatException();
//                        if ((!nsLat.equalsIgnoreCase("n")) && (!nsLat.equalsIgnoreCase("s"))) throw new DataFormatException();
//
//                        //System.out.println("LATITUDE: Deg: " + degreesLat + ", Min: " + minutesLat + ", Sec: " + secondsLat + ", Dir: " + nsLat);
//                        //System.out.print("(DD) ");
//
//                        // covert DMS to DD
//                        latitude = convertLatDMStoDD(degreesLat, minutesLat, secondsLat, nsLat);
//
//                        // check ranges again
//                        if ((latitude < -90) || (latitude > 90)) throw new DataFormatException();
//
//                    }catch(DataFormatException e){
//                        System.out.println("ERROR: values out of range");
//                        JOptionPane.showMessageDialog(null, "Latitude out of range!\n(0-90°, 0-59', 0-59.9\", N or S; max. 90° 0' 0\")");
//                    }catch(Exception e){
//                        System.out.println("ERROR: could not parse latitude");
//                        JOptionPane.showMessageDialog(null, "Please validate the prompted latitude (DMS or DD)!");
//                    }
//                }else{
//                    //System.out.println("(DMS+DD) ERROR: incorrect input");
//                    System.out.println("ERROR: incorrect latitude");
//                    JOptionPane.showMessageDialog(null, "Please validate the prompted latitude (DMS or DD)!");
//                }
//            }
//            return latitude;
//        }

//        /**
//         * parse longitude and return as double value
//         *
//         * @param String inputLong - string with longitude
//         * @return double - double value of longitude string
//         */
//        public static double parseLongitude(String inputLong){
//            // declarations
//            double longitude = 200;
//            int degreesLong, minutesLong;
//            double secondsLong;
//            String ewLong;
//            String[] coordLong;
//
//            // split input-string
//            coordLong = inputLong.split(" ");
//
//            if (coordLong.length != 4){
//                // prompted coordinates in DD (Decimal Degrees)
//                
//                //System.out.print("(DD) ");
//                try{
//                    // parse decimal degrees
//                    longitude = Double.parseDouble(inputLong);
//
//                    // check ranges
//                    if ((longitude < -180) || (longitude > 180)) throw new DataFormatException();
//
//                }catch(NumberFormatException e){
//                    System.out.println("ERROR: could not parse longitude");
//                    JOptionPane.showMessageDialog(null, "Please validate the prompted longitude (DMS or DD)!");
//                }catch (DataFormatException e){
//                    System.out.println("ERROR: longitude out of range");
//                    JOptionPane.showMessageDialog(null, "Longitude out of range\n(-180 -> 180)!");
//                }
//            }else{
//                if (coordLong.length == 4){
//                    // prompted coordinates in DMS (Degrees, Minutes, Seconds)
//                    
//                    //System.out.print("(DMS) ");
//                    try{                    
//                        // parse degrees, minutes, seconds
//                        degreesLong = Integer.parseInt(((coordLong[0]).split("°"))[0]);
//                        minutesLong = Integer.parseInt(((coordLong[1]).split("'"))[0]);
//                        secondsLong = Double.parseDouble(((coordLong[2]).split("\""))[0]);
//                        ewLong = (coordLong[3]).toString();
//
//                        // check ranges
//                        if ((degreesLong < 0) || (degreesLong > 90)) throw new DataFormatException();
//                        if ((minutesLong < 0) || (minutesLong >= 60)) throw new DataFormatException();
//                        if ((secondsLong < 0) || (secondsLong >= 60)) throw new DataFormatException();
//                        if ((degreesLong == 180) && ((minutesLong > 0) || (secondsLong > 0))) throw new DataFormatException();                    
//                        if ((!ewLong.equalsIgnoreCase("e")) && (!ewLong.equalsIgnoreCase("w"))) throw new DataFormatException();                    
//                        //System.out.println("LONGITUDE: Deg: " + degreesLong + ", Min: " + minutesLong + ", Sec: " + secondsLong + ", Dir: " + ewLong);
//                        //System.out.print("(DD) ");
//
//                        // covert DMS to DD
//                        longitude = convertLongDMStoDD(degreesLong, minutesLong, secondsLong, ewLong);                    
//                        
//                        // check ranges again
//                        if ((longitude < -180) || (longitude > 180)) throw new DataFormatException();
//
//                    }catch(DataFormatException e){
//                        System.out.println("ERROR: longitude out of range");
//                        JOptionPane.showMessageDialog(null, "Longitude of range!\n(0-180°, 0-59', 0-59.9\", E or W; max. 180° 0' 0\")");
//                    }catch(Exception e){
//                        System.out.println("ERROR: could not parse longitude");
//                        JOptionPane.showMessageDialog(null, "Please validate the prompted longitude (DMS or DD)!");
//                    }
//                }else{
//                    //System.out.println("(DMS+DD) ERROR: incorrect input");
//                    System.out.println("ERROR: incorrect longitude");
//                    JOptionPane.showMessageDialog(null, "Please validate the prompted longitude (DMS or DD)!");
//                }
//            }        
//            return longitude;
//        }
        
        /**
         * parse prompted serial port to test if given com-port descriptor exists
         *
         * @param String comPort - string with port
         * @return boolean - true: given port exists; false: given port not exists
         */
        public static boolean parseComPort(String comPort){
            for ( int i = 1; i <= 15; i++ ){
                // windows com-port description
                String port = "COM" + i;
                if (comPort.equals(port)) return true;
            }
            return false;
        }
        
//        /**
//         * parse search input and return as parameter-array
//         *
//         * @param String searchTextField - string search input
//         * @return String[] - search parameter: [0]: place name, [1]: postal code, [2]: country code; place name or postal code are needed, country code is optional in both variants
//         */
//        public static String[] parseSearchInput(String searchTextField){
//            // search parameter: [0]: place name, [1]: postal code, [2]: country code
//            // place name or postal code are needed, country code is optional in both variants
//            String[] parameter = new String[3];
//            String input[] = searchTextField.split(", ");
//            if (input.length == 0){
//                JOptionPane.showMessageDialog(null, "Input must not be empty!");
//                return null;
//            }
//            if (input[0].equals("") || input[0].equals(" ")){
//                JOptionPane.showMessageDialog(null, "Place name or postal code must not be empty!");                
//                return null;
//            }
//            if (input.length > 2){
//                JOptionPane.showMessageDialog(null, "More than two search attributes.\nPlease validate your input!");
//                return null;
//            }            
//            if (containsNumbers(input[0])){
//                // input could be a postal code, because place names usually do not contain numbers                
//                parameter[0] = "";
//                parameter[1] = convertStringToUTF8(input[0]);
//                parameter[2] = "";
//            }else{
//                // input must be a place name, because usually all postal codes contain numbers
//                parameter[0] = convertStringToUTF8(input[0]);
//                parameter[1] = "";
//                parameter[2] = "";
//            }            
//            if (input.length == 2){
//                // second attribute could be a country code
//                if (!(input[1].length() == 2)){
//                    // no valid code, because country codes only contain 2 characters (ISO-3166)
//                    JOptionPane.showMessageDialog(null, "Country code has not exactly two characters.\nPlease correct your input!");
//                    return null;
//                }
//                // test if input string only contains letters and no other characters
//                String cCode = input[1].toUpperCase();
//                if (isLetter(cCode.charAt(0)) && isLetter(cCode.charAt(1))){
//                    parameter[2] = convertStringToUTF8(cCode);
//                }else{
//                    JOptionPane.showMessageDialog(null, "Country code must only contain letters.\nPlease correct your input!");
//                    return null;
//                }
//            }
//            
//            return parameter;
//        }
        
    
    // CONVERTER/ADAPTER ((GPS) Coordinates)
        
//        /**
//         * convert Coordinate to GeoPosition
//         *
//         * @param Coordinate gps - coordinate-object to convert
//         * @return GeoPosition - geoposition-object of the coordinate-object
//         */
//        public static GeoPosition convertGpsToGeoPos(Coordinate gps){
//           return new GeoPosition(gps.getLatitude(), gps.getLongitude()); 
//        }
        
//        /**
//         * convert GeoPosition to GPSCoordinate
//         *
//         * @param GeoPosition geoPos - geoposition-object to convert
//         * @return GPSCoordinate - gpscoordinate-object of the geoposition-object
//         */
//        public static GPSCoordinate convertGeoPosToGps(GeoPosition geoPos){
//            return new GPSCoordinate(0, geoPos.getLatitude(), geoPos.getLongitude(), 0.0f);
//        }
        
        /**
         * convert GPS-Coordinates from DMS (Degrees, Minutes, Seconds) to DD (Decimal Degrees)
         *
         * @param int latDeg - latitude degree value
         * @param int latMin - latitude minute value
         * @param double latSec - latitude second value
         * @param String latDir - latitude direction (north or south)
         * @param int longDeg - longitude degree value
         * @param double longMin - longitude minute value
         * @param double longSec - longitude second value
         * @param String longDir - longitude direction (west or east)
         * @return double[] - double values for given latitude and longitude ([0]: latitude, [1]: longitude)
         */
        public static double[] convertDMStoDD(int latDeg, int latMin, double latSec, String latDir,
                                              int longDeg, int longMin, double longSec, String longDir){
            double[] coord = new double[2];
            coord[0] = convertLatDMStoDD(latDeg, latMin, latSec, latDir);
            coord[1] = convertLongDMStoDD(longDeg, longMin, longSec, longDir);
            return coord;
        }

        /**
         * convert latitude from DMS (Degrees, Minutes, Seconds) to DD (Decimal Degrees)
         *
         * @param int latDeg - latitude degree value
         * @param int latMin - latitude minute value
         * @param double latSec - latitude second value
         * @param String latDir - latitude direction (north or south)
         * @return double - double value for given latitude
         */
        public static double convertLatDMStoDD(int latDeg, int latMin, double latSec, String latDir){
            double latitude = latDeg + (latMin/new Double(60)) + (latSec/new Double(3600));
            if (latDir.equalsIgnoreCase("s")) latitude = -latitude;
            return latitude;
        }

        /**
         * convert longitude from DMS (Degrees, Minutes, Seconds) to DD (Decimal Degrees)
         *
         * @param int longDeg - longitude degree value
         * @param double longMin - longitude minute value
         * @param double longSec - longitude second value
         * @param String longDir - longitude direction (west or east)
         * @return double - double values for given longitude
         */
        public static double convertLongDMStoDD(int longDeg, int longMin, double longSec, String longDir){
            double longitude = longDeg+(longMin/new Double(60)) + (longSec/new Double(3600));
            if (longDir.equalsIgnoreCase("w")) longitude = -longitude;
            return longitude;
        }

//        /**
//         * convert GPS-Coordinates from DD (Decimal Degrees) to DMS (Degrees, Minutes, Seconds)
//         *
//         * @param double latitude - longitude double value
//         * @param double longitude - longitude double value
//         * @return String[] - string representation (degrees, minutes, seconds, direction) of given latitude and longitude values ([0]: latitude, [1]: longitude)
//         */
//        public static String[] convertDDtoDMS(double latitude, double longitude){
//            String[] coord = new String[2];
//            coord[0] = convertLatDDtoDMS(latitude);
//            coord[1] = convertLongDDtoDMS(longitude);
//            return coord;
//        }

        /**
         * convert latitude from DD (Decimal Degrees) to DMS (Degrees, Minutes, Seconds)
         *
         * @param double latitude - longitude double value
         * @return String - string representation (degrees, minutes, seconds, direction) of given latitude
         */
        public static String convertLatDDtoDMS(double latitude){
            int latDeg, latMin;
            double latSec;
            char lat = 'N';
            String deg = "", min = "", sec ="";

            if (latitude < 0){
                lat = 'S';
                latitude = -latitude;
            }
            latDeg = new Double(latitude).intValue();
            latitude = 60 * (latitude - latDeg);
            latMin = new Double(latitude).intValue();
            latitude = 60 * (latitude - latMin);
            latSec = latitude;

            if (latDeg < 10){
                deg = "0";
            }
            deg = deg + Integer.toString(latDeg);
            if (latMin < 10){
                min = "0";
            }
            min = min + Integer.toString(latMin);
            if (latSec < 10){
                sec = "0";
            }
            sec = sec + Double.toString(roundValue2(latSec));

            return (deg + "° " + min + "' " + sec + "\" " + lat);
        }

//        /**
//         * convert longitude from DD (Decimal Degrees) to DMS (Degrees, Minutes, Seconds)
//         *
//         * @param double longitude - longitude double value
//         * @return String - string representation (degrees, minutes, seconds, direction) of given longitude
//         */
//        public static String convertLongDDtoDMS(double longitude){
//            int longDeg, longMin;
//            double longSec;
//            char lon = 'E';
//            String deg = "", min = "", sec ="";
//
//            if (longitude < 0){
//                lon = 'W';
//                longitude = -longitude;
//            }
//
//            longDeg = new Double(longitude).intValue();
//            longitude = 60 * (longitude - longDeg);
//            longMin = new Double(longitude).intValue();
//            longitude = 60 * (longitude - longMin);
//            longSec = longitude;
//
//            if (longDeg < 10){
//                deg = "0";
//            }
//            deg = deg + Integer.toString(longDeg);
//            if (longMin < 10){
//                min = "0";
//            }
//            min = min + Integer.toString(longMin);
//            if (longSec < 10){
//                sec = "0";
//            }
//            sec = sec + Double.toString(roundValue2(longSec));
//
//            return (deg + "° " + min + "' " + sec + "\" " + lon);
//        }
//        
//        /**
//         * test and adapt the longitude value of a GeoPosition (can be >180° because of map shifting)
//         *
//         * @param GeoPosition pos - geoposition-object of a given position
//         * @return GeoPosition - geoposition-object of a given position with corrected longitude value
//         */
//        public static GeoPosition adaptLongitude(GeoPosition pos){
//            if (pos.getLongitude() > 180){
//                return adaptLongitude(new GeoPosition(pos.getLatitude(),(pos.getLongitude() - 360)));
//            }
//            if (pos.getLongitude() < -180){
//                return adaptLongitude(new GeoPosition(pos.getLatitude(),(pos.getLongitude() + 360)));
//            }
//            return pos;
//        }      
        
    
    // TIME AND DATE
        
        /**
         * get current System-Date
         *
         * @return Date - current date as date-object
         */
        public static Date getCurrentDate(){
            return new Date();
        }
        
        /**
         * get date as string from date-object
         *
         * @param Date date - date-object
         * @return String - string representation of the date (yyyy/MM/dd)
         */
        public static String getDate(Date date){
           DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
           return ((dateFormat.format(date)).split(" "))[0];            
        }
        
        /**
         * get time as string from date-object
         *
         * @param Date date - date-object
         * @return String - string representation of the time (HH:mm:ss)
         */
        public static String getTime(Date date){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return ((dateFormat.format(date)).split(" "))[1];            
        }
        
        /**
         * convert int value to time string
         *
         * @param int time - time in seconds as int value
         * @return String - string representation of the time (HH:mm:ss)
         */
        public static String convertIntToTime(int time){
            int hour = (time / 3600);
            int minutes = ((time - (hour*3600)) / 60);
            int seconds = time - (hour*3600) - (minutes*60);        

            hour = hour % 24;
            minutes = minutes % 60;
            seconds = seconds % 60;
            
            String gpsHour = java.lang.Integer.toString(hour);
            if (hour < 10) gpsHour = "0" + gpsHour;
            if (hour == 24) gpsHour = "00";

            String gpsMinutes = java.lang.Integer.toString(minutes);
            if (minutes < 10) gpsMinutes = "0" + gpsMinutes;

            String gpsSeconds = java.lang.Integer.toString(seconds);
            if (seconds < 10) gpsSeconds = "0" + gpsSeconds;

            return new String(gpsHour + ":" + gpsMinutes + ":" + gpsSeconds);       
        }
        
        /**
         * convert time string to int value
         *
         * @param String time - time as string representation
         * @return int - int value in seconds of the given time string
         */
        public static int convertTimeToInt(String time){
            String[] values = time.split(":");
            return ((Integer.parseInt(values[0]) * 3600)+(Integer.parseInt(values[1]) * 60)+(Integer.parseInt(values[2])));
        }
                
     
    // OTHER

        /**
         * round value (one position after decimal point)
         *
         * @param double value - non-rounded double value
         * @return int - rounded double value (one position after decimal point)
         */
        public static double roundValue1(double value){
            return (double)(((int)((value + 0.05)*10))/10.0);
        }

        /**
         * round value (two positions after decimal point)
         *
         * @param double value - non-rounded double value
         * @return int - rounded double value (two positions after decimal point)
         */
        public static double roundValue2(double value){
            return (double)(((int)((value + 0.005)*100))/100.00);
        }

        /**
         * double to rounded int
         *
         * @param double value - non-rounded double value
         * @return int - rounded int value
         */
        public static int doubleToInt(double value){
            return ((int) (value + 0.5));
        }

        /**
         * replace point with comma in double values
         *
         * @param double value - ouble value with point as decimal point
         * @return String - string representation of the double value with comma instead of point
         */
        public static String pointToComma(double value){
            return Double.toString(value).replace(".", ",");
        }
        
        /**
         * test for numbers in a given string
         *
         * @param String text - text for searching
         * @return boolean - true: text contains numbers, false: text not contains numbers
         */
        public static boolean containsNumbers(String text){
            for (int i=0; i<text.length(); i++){
                if (((int) text.charAt(i) >= 48) && ((int) text.charAt(i) <= 57)){
                    return true;
                }
            }
            return false;
        }
        
        /**
         * test if given character is a letter
         *
         * @param char character - character to test
         * @return boolean - true: characters is a letter, false: character is not a letter
         */
        public static boolean isLetter(char character){
            if (((int) character >= 65) && ((int) character <= 90)){
                return true;
            }else{
                return false;
            }
        }
        
        /**
         * convert String to UTF-8
         *
         * @param String text - text to convert
         * @return String - given string in utf-8
         */
        public static String convertStringToUTF8(String text){
            try{
                return java.net.URLEncoder.encode(text, "UTF-8"); 
            }catch(UnsupportedEncodingException uee){
                // encoding not supported
                return text; 
            }          
        }
        
        /**
         * create hashset from arraylist
         *
         * @param ArrayList list - list to create a hashset from
         * @return HashSet - given arraylist as hashset
         */
        public static HashSet getSetFromArrayList(ArrayList list){
            HashSet set = new HashSet();
            for (int i = 0; i < list.size(); i++){
                set.add(list.get(i));
            }            
            return set;
        }   

}
