package de.ulb.marc2geo.core;

/**
 * @author Jim Jones
 */

public class GlobalSettings {


	private static String baseResourceURI = "http://lobid.org/resource/";
	private static String baseGraphURI = "http://ulb.uni-muenster.de/context/karten/";
	private static String updateEndpoint = "http://giv-lodum.uni-muenster.de:8081/parliament/sparql";
	private static String noImageURL = "https://upload.wikimedia.org/wikipedia/de/d/d6/KeinBildVorhanden.jpg";
	private static String noPresenationURL = "https://en.wikipedia.org/wiki/HTTP_404";
	private static String timeBaseURL = "http://ulb.uni-muenster.de/resource/time/";
	private static String geometryBaseURL = "http://ulb.uni-muenster.de/resource/geometry/";
	private static String defaultCRS = "http://www.opengis.net/def/crs/EPSG/0/4326";
	private static String databaseHost = "giv-lodum.uni-muenster.de";
	private static String databaseName = "transfer";
	private static String databaseUser = "jones";
	private static String databasePassword = "";

	
	public static String getDatabaseHost() {

		return databaseHost;
		
	}
	
	public static String getDatabaseName() {

		return databaseName;
		
	}
	
	public static String getDatabaseUser() {

		return databaseUser;
		
	}
	
	public static String getDatabasePassword() {

		return databasePassword;
		
	}
	
	public static String getCRS() {

		return defaultCRS;
		
	}
	
	public static String getGeometryURL() {

		return geometryBaseURL;
		
	}

	public static String getTimeURL() {

		return timeBaseURL;
		
	}
	
	public static String getNoPresentationURL() {

		return noPresenationURL;
		
	}

	
	public static String getBaseURI() {

		return baseResourceURI;
		
	}
	

	public static String getEndpoint(){
		
		return updateEndpoint;
		
	}
	
	public static String getGraphBaseURI(){
		
		return baseGraphURI;
		
	}
	
	public static String getNoImageURL(){
		
		return noImageURL;
		
	}
}
