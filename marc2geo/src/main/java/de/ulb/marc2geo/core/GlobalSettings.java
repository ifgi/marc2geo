package de.ulb.marc2geo.core;

/**
 * @author Jim Jones
 */

public class GlobalSettings {


	private static String baseResourceURI = "http://lobid.org/resource/";
	private static String baseGraphURI = "http://ulb.uni-muenster.de/context/karten/";
	private static String updateEndpoint = "http://localhost:8083/parliament/sparql";
	private static String noImageURL = "https://upload.wikimedia.org/wikipedia/de/d/d6/KeinBildVorhanden.jpg";
	private static String noPresenationURL = "https://en.wikipedia.org/wiki/HTTP_404";
	

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
