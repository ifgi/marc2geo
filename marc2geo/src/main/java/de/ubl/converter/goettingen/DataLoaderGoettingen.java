package de.ubl.converter.goettingen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

public class DataLoaderGoettingen {

	private static Logger logger = Logger.getLogger("DataLoader-Göttingen");

	
	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;
		MySQLConnector cnn = new MySQLConnector();
		
				
		try {

			statement =  cnn.getConnection().createStatement();
			logger.info("Loading data from database...");
			result = statement.executeQuery("SELECT * FROM transfer.goettingen_dezember2015");
			
			
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

				MapRecord map = this.parseMARC21(data.getString("xml"));

				if(map.getYear() != null && 
				   map.getTitle() != null &&
				   map.getGeometry() != null &&
				   map.getHT() != null &&
				   map.getURI() != null ){

					result.add(map);
					valid++;


				} else {

					//logger.error("Map [" + map.getId() + "] does not contain all required properties. This map is incomplete and therefore won't be stored.");
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

	
	private void writeLogEntry(String entry){
		
		try {
			
			StringBuffer buffer = new StringBuffer();
			buffer.append(entry+"\n");
			
			FileOutputStream fileStream = new FileOutputStream(new File("/home/jones/delete/invalid_maps.log"),true);
			
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
				logger.error("No title for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No title for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map ID
			 */
			expr = xpath.compile("//record/controlfield[@tag='001']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);		    
				result.setHT(currentItem.getTextContent());
			} else {
				result.setHT(null);
				logger.error("No ID for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No ID for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
			}


			/**
			 * Map URI
			 */
			//expr = xpath.compile("//record/datafield[@tag='035']/subfield[@code='a']");
			expr = xpath.compile("//record/controlfield[@tag='001']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);		    
				//(DE-599)
				result.setURI("https://opac.sub.uni-goettingen.de/"+currentItem.getTextContent());
				//result.setUri(GlobalSettings.getBaseURI() + currentItem.getTextContent().replace("(", "").replace(")",""));
			} else {
				result.setURI(null);
				logger.error("No identifier for map (to build URI): " + result.getHT() + " \"" + result.getTitle() + "\".");
				this.writeLogEntry("No identifier for map (to build URI): " + result.getHT() + " \"" + result.getTitle() + "\".");
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
				//logger.warn("No description for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Geometry
			 */
			expr = xpath.compile("//record/datafield[@tag='255']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			String coordinates= new String();
			
			if(nl.getLength()!=0){

				try {
					
				Node currentItem = nl.item(0);
				//coordinates = currentItem.getTextContent().substring(currentItem.getTextContent().indexOf("(")+1,currentItem.getTextContent().indexOf(")")); 
				coordinates = currentItem.getTextContent();
								
				if(currentItem.getTextContent().equals("(E 013 20--E 014 20/N 051 30--N 051 00).")){
					System.out.println("!!!");
				}
				
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
				
				String	wkt = "<" + GlobalSettings.getCRS() + ">POLYGON((" + west + " " + north + ", " + east + " " + north + ", " + east + " " + south + ", " + west + " " + south + ", " + west + " " + north + "))";
				result.setGeometry(wkt);
				
				
				} catch (Exception e) {
					
					logger.error("Invalid coordinates found for map: " + result.getHT() + " \"" + result.getTitle() + "\": "+ coordinates);
								
					this.writeLogEntry("Invalid coordinates found for map: " + result.getHT() + " \"" + result.getTitle() + "\": "+ coordinates);
					result.setGeometry(null);
				}

			} else {

				logger.error("No coordinates found for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
				//this.writeLogEntry("No coordinates found for map: " + result.getId() + " \"" + result.getTitle() + "\".");
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
				//logger.warn("No paper map size for map: " + result.getId() + " \"" + result.getTitle() + "\".");
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
						this.writeLogEntry("Invalid year for map: " + result.getHT() + " \"" + result.getTitle() + "\": " + tmpNode);
						logger.error("Invalid year for map: " + result.getHT() + " \"" + result.getTitle() + "\": " + tmpNode);
						
						
					} else {
						
						result.setYear(""+Integer.parseInt(tmpYear));
					}
					
				}else {
					
					result.setYear(null);
					logger.error("No year for map: " + result.getHT() + " \"" + result.getTitle() + "\".");
					
				}

			} catch (Exception e) {

				result.setYear(null);
				logger.error("Invalid year for map: " + result.getHT() + " \"" + result.getTitle() + "\": " + tmpNode);
				this.writeLogEntry("Invalid year for map: " + result.getHT() + " \"" + result.getTitle() + "\": " + tmpNode);

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
				result.setImage("https://upload.wikimedia.org/wikipedia/de/c/c9/Uni_G%C3%B6ttingen_Siegel.png");
				result.setThumbnail("https://upload.wikimedia.org/wikipedia/de/c/c9/Uni_G%C3%B6ttingen_Siegel.png");
				//logger.warn("No image for map: " + result.getId() + " \"" + result.getTitle() + "\".");
			}

			/**
			 * Map Presentation
			 */
			result.setPresentation("https://opac.sub.uni-goettingen.de/DB=1/FKT=/FRM=/IMPLAND=Y/LNG=DU/LRSET=/MATC=/SET=/SID=/SRT=YOP/TTL=/CMD?ACT=SRCHA&TRM=ppn+"+result.getHT());

			/**
			 * Map Scale
			 */
			expr = xpath.compile("//record/datafield[@tag='255']/subfield[@code='a']");
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

//	/**
// 	 * @param bbox -> "(E 016 05 -E 016 20 /N 047 45 -N 047 30) or (W 043 00--W 042 30/S 012 30--S 013 00)"  
//	 * @return wkt -> "POLYGON(16.2 47.45, 16.05 47.45, 16.05 47.3, 16.2 47.3, 16.2 47.45)"
//	 */	
//	public String getWKTPolygon(String bbox){
//					
//		/**
//		 * (E 016 05 -E 016 20 /N 047 45 -N 047 30)
//		 */
//		String coordinates = bbox.substring(bbox.indexOf("(")+1,bbox.indexOf(")")); 
//		
//		double east = Double.parseDouble(coordinates.split(" ")[1] + "." + coordinates.split(" ")[2]);
//		double west = Double.parseDouble(coordinates.split(" ")[4] + "." + coordinates.split(" ")[5]);
//		double north = Double.parseDouble(coordinates.split(" ")[7] + "." + coordinates.split(" ")[8]);
//		double south = Double.parseDouble(coordinates.split(" ")[10] + "." + coordinates.split(" ")[11]);
//		
//		String wkt = "POLYGON(" + west + " " + north + ", " + east + " " + north + ", " + east + " " + south + ", " + west + " " + south + ", " + west + " " + north + ")"; 
//		
//		/**
//		 * (W 043 00--W 042 30/S 012 30--S 013 00)
//		 */				
//		
//		return wkt;
//	}	
	
	/**
	 * 
	 * @param map
	 * @return SPARQL insert statement.
	 */
	public String getSPARQLInsert(MapRecord map, String mode){

		String SPARQLinsert = "";
		
		if (mode.equals("insert")) {
			
			SPARQLinsert = "\nINSERT DATA {\n " ;
			SPARQLinsert = SPARQLinsert + "    GRAPH <http://ulb.uni-muenster.de/context/karten/goettingen> {\n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> a <http://www.geographicknowledge.de/vocab/maps#Map> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#medium> <http://www.geographicknowledge.de/vocab/maps#Paper> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#digitalImageVersion> <" + map.getImage() + ">. \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapSize> '" + map.getMapSize() + "'^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#title> '" + map.getTitle() + "'^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#presentation> <" + map.getPresentation() + "> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapsTime> <" + GlobalSettings.getTimeURL() + map.getHT() + "> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getHT() + "> a <http://www.w3.org/2006/time#Instant> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getHT() + "> <http://www.w3.org/2001/XMLSchema#gYear> '" + map.getYear() + "'^^<http://www.w3.org/2001/XMLSchema#gYear> .\n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#hasScale> '" + map.getScale() + "'^^<http://www.w3.org/2001/XMLSchema#string>. \n" ;
	
			if(map.getGeometry() != null){
	
				SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapsArea> <" + GlobalSettings.getGeometryURL() + map.getHT() + "> .\n";
				SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getHT() + "> a <http://www.opengis.net/ont/geosparql/1.0#Geometry> . \n" ;			   
				SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getHT() + "> <http://www.opengis.net/ont/geosparql/1.0#asWKT> '"+ map.getGeometry() +"'^^<http://www.opengis.net/ont/geosparql#wktLiteral> . \n" ;
	
			}
	
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://purl.org/dc/terms/description> \"" + map.getDescription() + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
			SPARQLinsert = SPARQLinsert + "    } \n" ;
			SPARQLinsert = SPARQLinsert + "} \n" ;

		}
		
		if (mode.equals("dump")){
			
			SPARQLinsert = "\n " ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.geographicknowledge.de/vocab/maps#Map> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#medium> <http://www.geographicknowledge.de/vocab/maps#Paper> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#digitalImageVersion> <" + map.getImage() + ">. \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapSize> \"" + map.getMapSize() + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#title> \"" + map.getTitle().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#presentation> <" + map.getPresentation() + "> . \n" ;
			SPARQLinsert = SPARQLinsert + "			  <" + map.getURI() + "> <http://xmlns.com/foaf/0.1/thumbnail> <" + map.getThumbnail() + "> . \n" ;		
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapsTime> <" + GlobalSettings.getTimeURL() + map.getHT() + "> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getHT() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2006/time#Instant> . \n" ;
			SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getTimeURL() + map.getHT() + "> <http://www.w3.org/2001/XMLSchema#gYear> '" + map.getYear() + "'^^<http://www.w3.org/2001/XMLSchema#gYear> .\n" ;
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#hasScale> \"" + map.getScale() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. \n" ;
	
			if(map.getGeometry() != null){
	
				SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapsArea> <" + GlobalSettings.getGeometryURL() + map.getHT() + "> .\n";
				SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getHT() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/ont/geosparql/1.0#Geometry> . \n" ;			   
				SPARQLinsert = SPARQLinsert + "           <" + GlobalSettings.getGeometryURL() + map.getHT() + "> <http://www.opengis.net/ont/geosparql/1.0#asWKT> '"+ map.getGeometry() +"'^^<http://www.opengis.net/ont/geosparql#wktLiteral> . \n" ;
	
			}
	
			SPARQLinsert = SPARQLinsert + "           <" + map.getURI() + "> <http://purl.org/dc/terms/description> \"" + map.getDescription().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;

			
		}
		
		return SPARQLinsert;

	}

	public void dropNamedGraph(MapRecord map){

		String sparql = "DROP GRAPH <" + GlobalSettings.getGraphBaseURI() + map.getHT() + ">";

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

	public void createSpatiotemporalIndexes(ArrayList<MapRecord> maps){

		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < maps.size(); i++) {
			
			buffer.append("INSERT {} WHERE {<" + GlobalSettings.getGraphBaseURI() + maps.get(i).getHT() + "> <http://parliament.semwebcentral.org/pfunction#enableIndexing> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> }\n");
		}
		
		System.out.println(buffer.toString());
		

	}
	
	public void storeTriples(String sparql, String mode){

		
		try {

			if (mode.equals("insert")){
				
				String body = "update=" + URLEncoder.encode(sparql,"UTF-8");
	
				URL url = new URL( "http://giv-lodum.uni-muenster.de:8081/parliament/sparql" );
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
			
			}
			
			
			if(mode.equals("dump")){
			
				StringBuffer buffer = new StringBuffer();
				buffer.append(sparql);
			
				
				FileOutputStream fileStream = new FileOutputStream(new File("/home/jones/delete/output_goettingen_dezember2015-2.nt"),true);
				OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");
				
				writer.append(buffer.toString());
				writer.close();
								
				
			}

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
