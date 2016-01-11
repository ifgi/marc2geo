package de.ulb.istg.geodata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;

public class GeoNamesAnsichtskarten {

	private static Logger logger = Logger.getLogger("GeoNames-IStG Skript");
	private java.sql.Connection connect = null;
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {

		logger.info("Script started...");
		GeoNamesAnsichtskarten instance = new GeoNamesAnsichtskarten();
		
		instance.loadData();
		
		logger.info("Script finished!");
	}

	
	public Connection getConnection(){
		
		try {

			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://giv-lodum.uni-muenster.de:3306/transfer?user=jones&password=passwort");


		} catch (ClassNotFoundException e) {		
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connect;

	}

	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;	
				
		try {

			statement =  this.getConnection().createStatement();
			System.out.println("Loading data from MySQL database...\n");
			
			result = statement.executeQuery("SELECT * FROM transfer.istg_ansichtskarten ORDER BY stadt;");
			
			
			
			FileOutputStream fileStream = new FileOutputStream(new File("/home/jones/delete/geonames-istg_matching-ansichtskarten.csv"),true);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");
			
			long matches = 0;
			
			while (result.next()) {
				
				com.hp.hpl.jena.query.ResultSet coordinates = this.getCoordinatesFromGeoNames(result.getString("stadt")); 
	    		
				String csv = "";
				String stadtNr = "NO PK";//result.getString("StadtNr");
	    		String stadtName = result.getString("stadt");
				
				if(!coordinates.hasNext()){

		    		System.out.println(stadtNr+"@"+stadtName+"@NOT FOUND@NOT FOUND");
		    		csv=csv+stadtNr+"@"+stadtName+"@NOT FOUND@NOT FOUND\n";

				}
				
		        while (coordinates.hasNext()) {
		            
		        	QuerySolution soln = coordinates.nextSolution();   		
		    		String wkt = soln.getLiteral("?wkt").getLexicalForm().toString();
		    			    		
		    		if(!wkt.toUpperCase().contains("POINT")){
		    			
			    		System.out.println(stadtNr+"@"+stadtName+"@INVALID@INVALID");
			    		csv=csv+stadtNr+"@"+stadtName+"@INVALID@INVALID\n";
		    			
		    		} else {
		    			matches++;
		    			System.out.println(stadtNr+"@"+stadtName+"@"+wkt+"@=HYPERLINK(\"http://ifgi.uni-muenster.de/~j_jone02/istg/locator.html?wkt="+wkt+"\")");
			    		csv=csv+stadtNr+"@"+stadtName+"@"+wkt+"@=HYPERLINK(\"http://ifgi.uni-muenster.de/~j_jone02/istg/locator.html?wkt="+wkt+"\")\n";
		    		}
		    							
		        }
		        
		        writer.append(csv);
				
			}
			

			writer.close();
			System.out.println("\n");
			System.out.println("Matches: " + matches);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}
	
	private com.hp.hpl.jena.query.ResultSet getCoordinatesFromGeoNames(String placeName){
		
		String SPARQL = "SELECT ?feature ?label ?geometry ?wkt ?historicalName WHERE { " +
						"	?feature a ?type . " +
						"	?feature <http://www.w3.org/2000/01/rdf-schema#label> ?label . " +
						"	?feature <http://geovocab.org/geometry#geometry> ?geometry . " +
						"	?geometry <http://www.opengis.net/ont/geosparql#asWKT> ?wkt  " +
						"	FILTER (REGEX(STR(?label), '^"+placeName+"$')) " +
						"	FILTER ( ?type = <http://linkedgeodata.org/ontology/City> || " +
						"			 ?type = <http://linkedgeodata.org/ontology/Town> || " +
						"			 ?type = <http://linkedgeodata.org/ontology/Place>) " +
						"} LIMIT 1";
		
		Query query = QueryFactory.create(SPARQL);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedgeodata.org/sparql", query);
		com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
		
		
		
		return results;
		
	}
}
