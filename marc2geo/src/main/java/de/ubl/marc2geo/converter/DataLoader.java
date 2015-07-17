package de.ubl.marc2geo.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import de.ulb.marc2geo.core.GlobalSettings;
import de.ulb.marc2geo.core.MapRecord;
import de.ulb.marc2geo.infrastructure.MySQLConnector;
import org.apache.log4j.Logger;

public class DataLoader {

	private static Logger logger = Logger.getLogger("DataLoader");

	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;
		MySQLConnector cnn = new MySQLConnector();

		try {

			statement =  cnn.getConnection().createStatement();
			logger.info("Loading data from database...");
			result = statement.executeQuery("SELECT * FROM transfer.KATALOG");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.fatal("Error loading data from " + GlobalSettings.getDatabaseHost() + ":\n" + e.getMessage());
		}

		return result;

	}


	public ArrayList<MapRecord> getMaps(){

		ArrayList<MapRecord> result = new ArrayList<MapRecord>();
		ResultSet data = this.loadData();

		double start = System.currentTimeMillis();
		long valid = 0;
		long invalid = 0;

		try {

			while (data.next()) {

				MapRecord map = this.parseMARC21(data.getString("rawxml"));

				if(map.getYear() != null && 
				   map.getTitle() != null &&
				   map.getGeometry() != null &&
				   map.getId() != null &&
				   map.getUri() != null ){

					result.add(map);
					valid++;


				} else {

					logger.error("Map [" + map.getId() + "] does not contain all required properties. This map is incomplete and therefore won't be stored.");
					invalid++;
				}

			}

			double end = (System.currentTimeMillis() - start);			
			double milliseconds = end / 1000;
			int minutes = (int) milliseconds / 60;


			logger.info("\n\nValid Records: " + valid + "\nInvalid Records: " + invalid + "\nHealth Check finished in " + minutes + " minutes and " + (milliseconds -(minutes / 1000)) + " seconds. \n");

		} catch (SQLException e) {
			e.printStackTrace();
		}


		return result;
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
				result.setTitle(currentItem.getTextContent());
			} else {
				result.setTitle(null);
				logger.error("No title for map: " + result.getId() + " \"" + result.getTitle() + "\".");
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
			}


			/**
			 * Map URI
			 */
			expr = xpath.compile("//record/datafield[@tag='776']/subfield[@code='w']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);		    
				result.setUri(GlobalSettings.getBaseURI() + currentItem.getTextContent().replace("(", "").replace(")",""));
			} else {
				result.setUri(null);
				logger.error("No identifier for map (to build URI): " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Description
			 */
			expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setDescription(currentItem.getTextContent());
			} else {
				result.setDescription("");
				logger.warn("No description for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Geometry
			 */
			expr = xpath.compile("//record/datafield[@tag='245']/subfield[@code='n']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){

				Node currentItem = nl.item(0);

				/**
				 * Coordinates Format: 34 34 Rechts 56 46 Hoch
				 */

				if(currentItem.getTextContent().contains("Rechts")){

					String rechts = currentItem.getTextContent().substring(0,currentItem.getTextContent().indexOf("R")).trim();
					String latitudeSouthEast = rechts.split(" ")[1];
					String longitudeSouthEast = rechts.split(" ")[0];

					String hoch = currentItem.getTextContent().substring(currentItem.getTextContent().indexOf("s")+1,currentItem.getTextContent().indexOf("H")).trim();
					String latitudeNorthWest = hoch.split(" ")[1];
					String longitudeNorthWest = hoch.split(" ")[0];

					String wkt = "<" + GlobalSettings.getCRS() + ">POLYGON((" + hoch + "," + longitudeNorthWest + " " + latitudeSouthEast + "," + rechts + "," + longitudeSouthEast + " " + latitudeNorthWest + "," + hoch + "))";
					result.setGeometry(wkt);


				} else {

					logger.error("Unexpected coordinates format for map: " + result.getId() + " \"" + result.getTitle() + "\" > " + currentItem.getTextContent());
					result.setGeometry(null);

				}



			} else {

				logger.error("No coordinates found for map: " + result.getId() + " \"" + result.getTitle() + "\".");
				result.setGeometry(null);


			}

			/**
			 * Map Size
			 */
			expr = xpath.compile("//record/datafield[@tag='300']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setMapSize(currentItem.getTextContent());				
			}else {
				result.setMapSize("");
				logger.warn("No paper map size for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Year
			 */
			expr = xpath.compile("//record/datafield[@tag='260']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setYear(currentItem.getTextContent());
			}else {
				result.setYear(null);
				logger.error("No year for map: " + result.getId() + " \"" + result.getTitle() + "\".");
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
				result.setImage(GlobalSettings.getNoImageURL());
				logger.warn("No image for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Presentation
			 */
			expr = xpath.compile("//record/datafield[@tag='856']/subfield[@code='u']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setPresentation(currentItem.getTextContent());
			}else {
				result.setPresentation(GlobalSettings.getNoPresentationURL());
				logger.warn("No presentation URL for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}


			/**
			 * Map Scale
			 */
			expr = xpath.compile("//record/datafield[@tag='????']/subfield[@code='????']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setScale(currentItem.getTextContent());				
			}else {
				result.setScale("0:0000");
				logger.warn("No scale for map: " + result.getId() + " \"" + result.getTitle() + "\".");
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


	public String getSPARQLInsert(MapRecord map){

		String SPARQLinsert = "\nINSERT DATA {\n " ;
		SPARQLinsert = SPARQLinsert + "    GRAPH <" + GlobalSettings.getGraphBaseURI() + map.getId() + "> {\n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> a <http://www.geographicknowledge.de/vocab/maps#Map> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#medium> <http://www.geographicknowledge.de/vocab/maps#Paper> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#digitalImageVersion> <" + map.getImage() + ">. \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapSize> '" + map.getMapSize() + "'^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#title> '" + map.getTitle() + "'^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#presentation> <" + map.getPresentation() + "> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapsTime> <" + GlobalSettings.getTimeURL() + map.getId() + "> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getId() + "> a <http://www.w3.org/2006/time#Instant> . \n" ;
		SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getId() + "> <http://www.w3.org/2001/XMLSchema#gYear> '" + map.getYear() + "'^^<http://www.w3.org/2001/XMLSchema#gYear> .\n" ;
		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#hasScale> '" + map.getScale() + "'^^<http://www.w3.org/2001/XMLSchema#string>. \n" ;

		if(map.getGeometry() != null){

			SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://www.geographicknowledge.de/vocab/maps#mapsArea> <" + GlobalSettings.getGeometryURL() + map.getId() + "> .\n";
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getId() + "> a <http://www.opengis.net/ont/geosparql/1.0#Geometry> . \n" ;			   
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getId() + "> <http://www.opengis.net/ont/geosparql/1.0#asWKT> '"+ map.getGeometry() +"'^^<http://www.opengis.net/ont/geosparql#wktLiteral> . \n" ;

		}

		SPARQLinsert = SPARQLinsert + "           <" + map.getUri() + "> <http://purl.org/dc/terms/description> '" + map.getDescription() + "'^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		SPARQLinsert = SPARQLinsert + "    } \n" ;
		SPARQLinsert = SPARQLinsert + "} \n" ;

		return SPARQLinsert;

	}

	public void dropNamedGraph(MapRecord map){

		String sparql = "DROP GRAPH <" + GlobalSettings.getGraphBaseURI() + map.getId() + ">";

		try {

			String body = "update=" + URLEncoder.encode(sparql,"UTF-8");

			URL url = new URL( GlobalSettings.getEndpoint() );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod( "POST" );
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			writer.close();
			reader.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal("Error dropping named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getCause());
		} catch (MalformedURLException e) {			
			e.printStackTrace();
			logger.fatal("Error dropping named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Error dropping named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getMessage());			
		}

	}

	public void createSpatiotemporalIndexes(MapRecord map){

		String sparql = "INSERT {} WHERE {<" + GlobalSettings.getGraphBaseURI() + map.getId() + "> <http://parliament.semwebcentral.org/pfunction#enableIndexing> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> }";

		try {

			String body = "update=" + URLEncoder.encode(sparql,"UTF-8");

			URL url = new URL( GlobalSettings.getEndpoint() );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod( "POST" );
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			writer.close();
			reader.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal("Error creating indexes for named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getCause());
		} catch (MalformedURLException e) {			
			e.printStackTrace();
			logger.fatal("Error creating indexes for named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getCause());
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Error creating indexes for named graph at [" + GlobalSettings.getEndpoint() + "] >> " + e.getCause());			
		}

	}
	
	public void storeTriples(String sparql){

		try {

			String body = "update=" + URLEncoder.encode(sparql,"UTF-8");

			URL url = new URL( GlobalSettings.getEndpoint() );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod( "POST" );
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			writer.close();
			reader.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + GlobalSettings.getEndpoint() + "] >> " + e.getCause());
		} catch (MalformedURLException e) {			
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + GlobalSettings.getEndpoint() + "] >> " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Error storing triples at [" + GlobalSettings.getEndpoint() + "] >> " + e.getMessage());			
		}

	}
}
