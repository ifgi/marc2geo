package de.ulb.converter.muenster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import de.ulb.marc2geo.core.MapRecord;
import org.apache.log4j.Logger;

public class RDFConverterMuenster {

	
	private static String outputFile ="/home/jones/delete/Muenster/output_muenster_januar2016.nt"; 
	
	private static String logFile ="/home/jones/delete/Muenster/invalid_maps.log";
	private static String mapURI ="http://lobid.org/resource/";
	private static String defaultCRS ="http://www.opengis.net/def/crs/EPSG/0/4326";
	private static String noImageURL ="https://upload.wikimedia.org/wikipedia/de/d/d6/KeinBildVorhanden.jpg";
	private static String noPresentationURL = "http://cdn.speckyboy.com/wp-content/uploads/2010/03/four-oh-four_04.jpg";
	private static String SPARQLEndpoint = "";
	
	private static Logger logger = Logger.getLogger("RDF Converter-Münster");
	public static void main(String[] args) {
	
		RDFConverterMuenster loader = new RDFConverterMuenster();		
		ArrayList<MapRecord> maps = new ArrayList<MapRecord>(); 
		
		maps = loader.getMaps();
	
		
		logger.info("Generating RDF triples ...");
		
		for (int i = 0; i < maps.size(); i++) {
			
			loader.generateRDFTriples(loader.getSPARQLInsert(maps.get(i)));					
			
		}
		
		logger.info("RDF Triples generated at " + outputFile);
	}
	
	
	public Connection getConnection(){

		java.sql.Connection connect = null;
		
		try {

			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/disco2?user=disco2_u1&password=AbJeJuvUANDar2hS");

		} catch (ClassNotFoundException e) {		
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			
			logger.fatal("Error connecting to the database server >> " +  e.getMessage());
		}

		return connect;

	}

	
	private ResultSet loadMapsFromCentralDatabase(){

		Statement statement = null;
		ResultSet result = null;			
		
		logger.info("Loading maps (as MARC21) from the MySQL central database ...");
		
		try {

			statement =  this.getConnection().createStatement();
			result = statement.executeQuery(" SELECT k.isbn, x.rawxml " +
											" FROM KATALOGXML as x, " +
											"	  KATALOG as k " +
											" WHERE k.leader like '______e%' AND " +
											"	   x.katkey = k.katkey AND " +
											"	   k.isbn IS NOT NULL;");
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.fatal("Error loading data from database.\n" + e.getMessage());
		}

		return result;

	}


	public ArrayList<MapRecord> getMaps(){

		ArrayList<MapRecord> result = new ArrayList<MapRecord>();
		double start = System.currentTimeMillis();
		
		ResultSet data = this.loadMapsFromCentralDatabase();
			
		long valid = 0;
		long invalid = 0;

		try {

			while (data.next()) {

				MapRecord map = this.parseMARC21(data.getString("x.rawxml"));

				if(map.getYear() != null && 
				   map.getTitle() != null &&
				   map.getGeometry() != null &&
				   map.getId() != null &&
				   map.getUri() != null ){

					result.add(map);
					valid++;


				} else {

					invalid++;
				}

			}

			
			logger.info("\nEnriching maps' metadata with VL data ... \n");
			
			for (int i = 0; i < result.size(); i++) {
				
			}
			
			
			double end = (System.currentTimeMillis() - start);			
			double milliseconds = end / 1000;
			int minutes = (int) milliseconds / 60;


			logger.info("\n\nValid Records: " + valid + "\nInvalid Records: " + invalid + "\nOperation finished in " + minutes + " minutes and " + (milliseconds -(minutes / 1000)) + " seconds. \n");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}

	
	private void writeLogEntry(String entry){
		
		try {
			
			StringBuffer buffer = new StringBuffer();
			buffer.append(entry+"\n");
			
			FileOutputStream fileStream = new FileOutputStream(new File(logFile),true);
			
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");
			
			writer.append(buffer.toString());
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static double round(double value, int places) {
	    
		if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    
	    return bd.doubleValue();
	    
	}
	
		
	private MapRecord parseMARC21(String marc21) {

		MapRecord result = new MapRecord();

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputSource source = new InputSource(new StringReader(marc21));

			Document doc = builder.parse(source);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();

			/**
			 * Map Title
			 */
			XPathExpression expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='a']");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);	    
				result.setTitle(currentItem.getTextContent().replace("\"", "\'"));
			} else {
				result.setTitle(null);
				logger.error("No title for map: " + result.getId() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No title for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map ID
			 */
			expr = xpath.compile("//record/controlfield[@tag='001']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);		    
				result.setId(currentItem.getTextContent());
			} else {
				result.setId(null);
				logger.error("No ID for map: " + result.getId() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No ID for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}


			/**
			 * Map URI
			 */

			expr = xpath.compile("//record/datafield[@tag='035']/subfield[@code='a']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){

				Node currentItem = nl.item(0);
				String id = currentItem.getTextContent();
				
				id = id.substring(id.indexOf(")")+1, id.length());
				result.setUri(mapURI +  id);
				
			} else {
				result.setUri(null);
				logger.error("No identifier for map (to build URI): " + result.getId() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No identifier for map (to build URI): " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Description
			 */
			expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				
				Node currentItem = nl.item(0);
				result.setDescription(currentItem.getTextContent().replace("\"", "\'"));
				
			} else {

				result.setDescription("");

			}

			/**
			 * Map Geometry //<http://purl.org/dc/terms/references>
			 */
			expr = xpath.compile("//record/datafield[@tag='255']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			String coordinates= new String();
			
			if(nl.getLength()!=0){

				try {
					
				Node currentItem = nl.item(0);
				coordinates = currentItem.getTextContent();							
				
				double east = 0.0;
				double west = 0.0;
				double north = 0.0;
				double south = 0.0;
				
				String coordinateString;
	
				coordinateString = coordinates.replace("--", " -");			
				coordinateString = coordinateString.toUpperCase();
				coordinateString = coordinateString.replace("/", " /");
				coordinateString = coordinateString.replace("/", "");
				coordinateString = coordinateString.replace("(", "");
				coordinateString = coordinateString.replace(")", "");
				coordinateString = coordinateString.replace("[", "");
				coordinateString = coordinateString.replace("]", "");
				coordinateString = coordinateString.replace(".", "");
				coordinateString = coordinateString.replace("  ", " ");
				
				String[] array = coordinateString.trim().split(" ");
								
				for (int j = 0; j < array.length; j++) {
					
					if (array[j].equals("W")) {			
						
						west = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						west *= -1;						
						
					} else if (array[j].equals("-W")) {

						east = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						east *= -1;
						
					} else if (array[j].equals("S")) {
						
						south = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						south *= -1;
						
					} else if (array[j].equals("-S")) {
						
						north = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						north *= -1;
						
					} else if (array[j].equals("E")) {
						
						east = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
												
					} else if (array[j].equals("-E")) {
						
						west = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						
					} else if (array[j].equals("N")) {
						
						north = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
						
					} else if (array[j].equals("-N")) {
						
						south = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
													
					}
					
				}
					
				west = round(west,2); 
				east = round(east,2);
				south = round(south,2);
				north = round(north,2);
				
				String	wkt = "<" + defaultCRS + ">POLYGON((" + west + " " + north + ", " + east + " " + north + ", " + east + " " + south + ", " + west + " " + south + ", " + west + " " + north + "))";
				result.setGeometry(wkt);
				
				
				} catch (Exception e) {
					
					logger.error("Invalid coordinates found for map: " + result.getId() + " \"" + result.getTitle() + "\": "+ coordinates);
								
					this.writeLogEntry("Invalid coordinates found for map: " + result.getId() + " \"" + result.getTitle() + "\": "+ coordinates);
					result.setGeometry(null);
				}

			} else {

				logger.error("No coordinates found for map: " + result.getId() + " \"" + result.getTitle() + "\".");
				result.setGeometry(null);


			}

			/**
			 * Map Size
			 */
			expr = xpath.compile("//record/datafield[@tag='300']/subfield[@code='b']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){

				Node currentItem = nl.item(0);
				result.setMapSize(currentItem.getTextContent());
				
			}else {
				
				result.setMapSize("");

			}

			/**
			 * Map Year
			 */
			expr = xpath.compile("//record/datafield[@tag='260']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			String tmpNode ="";
			
			try {
			
				if(nl.getLength()!=0){
					Node currentItem = nl.item(0);
					tmpNode = currentItem.getTextContent();
					String tmpYear = currentItem.getTextContent().replace("[", "").toUpperCase();
					tmpYear = tmpYear.replace("]", "");
					tmpYear = tmpYear.replace(" ", "");
					tmpYear = tmpYear.replace("C", "");
					tmpYear = tmpYear.replace("-", "");
					tmpYear = tmpYear.replace("(", "");
					tmpYear = tmpYear.replace(")", "");
					tmpYear = tmpYear.replace("D", "");
					tmpYear = tmpYear.replace("L", "");
					tmpYear = tmpYear.replace(".", "");
					tmpYear = tmpYear.replace("A", "");
					tmpYear = tmpYear.replace("V", "");
					tmpYear = tmpYear.replace("O", "");
					tmpYear = tmpYear.replace("R", "");
					tmpYear = tmpYear.replace("S", "");
					tmpYear = tmpYear.replace("/", "");
					tmpYear = tmpYear.replace("C/", "");
					tmpYear = tmpYear.replace("I", "");
					tmpYear = tmpYear.replace("U", "");
					tmpYear = tmpYear.replace("M", "");
					tmpYear = tmpYear.replace("N", "");
					tmpYear = tmpYear.replace("H", "");
					tmpYear = tmpYear.replace("O", "");
					tmpYear = tmpYear.replace("?", "");
					
					if(tmpYear.trim().length()!=4){
						
						result.setYear(null);
						this.writeLogEntry("Invalid year for map: " + result.getId() + " \"" + result.getTitle() + "\": " + tmpNode);
						logger.error("Invalid year for map: " + result.getId() + " \"" + result.getTitle() + "\": " + tmpNode);
						
						
					} else {
						
						result.setYear(""+Integer.parseInt(tmpYear));
					}
					
				}else {
					
					result.setYear(null);
					logger.error("No year for map: " + result.getId() + " \"" + result.getTitle() + "\".");
					
				}

			} catch (Exception e) {

				result.setYear(null);
				logger.error("Invalid year for map: " + result.getId() + " \"" + result.getTitle() + "\": " + tmpNode);
				this.writeLogEntry("Invalid year for map: " + result.getId() + " \"" + result.getTitle() + "\": " + tmpNode);

			}

			/**
			 * Map Image
			 * TODO: Change Fake Tag. Currently there is no way to retrieve images from resources!
			 */
			expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='fakebild']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setImage(currentItem.getTextContent());				
			}else {
				result.setImage(noImageURL);
				logger.warn("No image for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Presentation
			 */
			expr = xpath.compile("//record/datafield[@tag='035']/subfield[@code='a']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				String id = currentItem.getTextContent();
				
				id = id.substring(id.indexOf(")")+1, id.length());
				result.setPresentation(mapURI +  id);
			}else {
				result.setPresentation(noPresentationURL);
				logger.warn("No presentation URL for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}
			
			/**
			 * Map Scale
			 */
			expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='x']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				
				Node currentItem = nl.item(0);

				String scale = currentItem.getTextContent();
				result.setScale(scale);
								
			}else {
				
				result.setScale("0:0000");
				
			}			


		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @param map
	 * @return SPARQL insert statement.
	 */
	public String getSPARQLInsert(MapRecord map){

		String SPARQLinsert = "";
		//prefix owl: <http://www.w3.org/2002/07/owl#> 
		SPARQLinsert = "\n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.geographicknowledge.de/vocab/maps#Map> . \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#ID> \""+ map.getId() +"\". \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#medium> <http://www.geographicknowledge.de/vocab/maps#Paper> . \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#digitalImageVersion> <" + map.getImage() + ">. \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapSize> \"" + map.getMapSize() + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#title> \"" + map.getTitle().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#presentation> <" + map.getPresentation() + "> . \n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapsTime> _:" + map.getId() + " . \n" ;
		SPARQLinsert = SPARQLinsert + "_:" + map.getId() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2006/time#Instant> . \n" ;
		SPARQLinsert = SPARQLinsert + "_:" + map.getId() + " <http://www.w3.org/2001/XMLSchema#gYear> '" + map.getYear() + "'^^<http://www.w3.org/2001/XMLSchema#gYear> .\n" ;
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#hasScale> \"" + map.getScale() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. \n" ;
		
		if(map.getGeometry() != null){
		
			SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapsArea> _:" + map.getId() + " .\n";
			SPARQLinsert = SPARQLinsert + "_:" + map.getId() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/ont/geosparql/1.0#Geometry> . \n" ;			   
			SPARQLinsert = SPARQLinsert + "_:" + map.getId() + " <http://www.opengis.net/ont/geosparql/1.0#asWKT> '"+ map.getGeometry() +"'^^<http://www.opengis.net/ont/geosparql#wktLiteral> . \n" ;
		
		}
		
		SPARQLinsert = SPARQLinsert + "<" + map.getUri() + "> <http://purl.org/dc/terms/description> \"" + map.getDescription().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		
		return SPARQLinsert;

	}
	
	public void generateRDFTriples(String sparql){

		try {

			StringBuffer buffer = new StringBuffer();
			buffer.append(sparql);

			FileOutputStream fileStream = new FileOutputStream(new File(outputFile),true);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");

			writer.append(buffer.toString());
			writer.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + SPARQLEndpoint + "] >> " + e.getCause());
		} catch (MalformedURLException e) {			
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + SPARQLEndpoint + "] >> " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + SPARQLEndpoint + "] >> " + e.getMessage());			
		}

	}
}
