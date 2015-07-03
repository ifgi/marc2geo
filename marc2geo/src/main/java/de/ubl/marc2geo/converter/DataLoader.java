package de.ubl.marc2geo.converter;

import java.io.IOException;
import java.io.StringReader;
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
import de.ulb.marc2geo.infrastructure.MySQLConnector;

public class DataLoader {

	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;
		MySQLConnector cnn = new MySQLConnector();

		try {

			statement =  cnn.getConnection().createStatement();
			result = statement.executeQuery("select * from transfer.KATALOG");

		} catch (SQLException e) {

			e.printStackTrace();
		}

		return result;

	}


	public ArrayList<MapRecord> getMaps(){

		ArrayList<MapRecord> result = new ArrayList<MapRecord>();
		ResultSet data = this.loadData();


		try {

			while (data.next()) {

				MapRecord map = new MapRecord();

				map = parseMAR21(data.getString("rawxml"));

				//map.setTitle(data.getString("title"));
				//map.setGeometry(data.getString("rawxml"));			

				result.add(map);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}


		return result;
	}


	private MapRecord parseMAR21(String marc21) {

		MapRecord result = new MapRecord();

		/**
		 
		Required fields:
		
		- Map URI
		- Map title
		- Map scale
		- Map geometry 
		- Map image
		- Map year
		- Map description
		 */


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
				//String key = currentItem.getAttributes().getNamedItem("code").getNodeValue();			    
				result.setTitle(currentItem.getTextContent());
			} else {
				result.setTitle("No Title");
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
				result.setDescription("No Description");
			}
			
			/**
			 * Map Scale
			 */
			expr = xpath.compile("//record/datafield[@tag='300']/subfield[@code='c']");
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);
				result.setScale(currentItem.getTextContent());				
			}else {
				result.setScale("No Scale");
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


	private String getSPARQLInsert(MapRecord map){

		//TODO: Convert every record into a SPARQL INSERT statement.

		return "";

	}

	private void storeTriples(String sparql){

		//TODO: Create function to store triples into Parliament.

	}
}
