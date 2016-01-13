package de.ulb.converter.muenster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GoettingenMuensterISBN {

	private static Logger logger = Logger.getLogger("DataLoader-Münster/Göttingen");
	private java.sql.Connection connect = null;
	/**
	 * @param args
	 */

	public static void main(String[] args) {

		GoettingenMuensterISBN instance = new GoettingenMuensterISBN();

		instance.loadData();

	}


	public Connection getConnection(){

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

	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;	

		try {

			statement =  this.getConnection().createStatement();
			logger.info("Loading data from MySQL database...\n");
			result = statement.executeQuery("select k.isbn, x.rawxml from KATALOGXML as x, KATALOG as k where k.leader like '______e%' and x.katkey = k.katkey;");

			long matches = 0;
			long doesntMatch = 0;

			while (result.next()) {

				String coordinates = this.getCoordinatesFromGoettingen(result.getString("isbn")); 

				if(!coordinates.equals("")){

					matches++;
					//Werner :-)
					System.out.println("ISBN: " + result.getString("isbn") + " >> " + coordinates);

				} else {

					doesntMatch++;

				}

			}

			System.out.println("\n");
			logger.info("Matches: " + matches);
			logger.info("Total Records: " + (matches+doesntMatch));

		} catch (SQLException e) {

			e.printStackTrace();
			logger.fatal("Error loading data from MySQL database: \n" + e.getMessage());

		}

		return result;

	}

	private String getCoordinatesFromGoettingen(String isbn) {
		String coordinates="";         
		try {
			URL url = new URL("http://sru.gbv.de/gvk?version=1.2&operation=searchRetrieve&query=pica.isb%3D"
					+ isbn + "+sortby+year&maximumRecords=500&startRecord=1");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();           
			org.w3c.dom.Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(url.openStream()));                                   
			XPath xpath = XPathFactory.newInstance().newXPath();                   
			NodeList subfields = (NodeList) xpath.evaluate("//records/record/datafield[@tag='255']/subfield[@code='c']", document,
					XPathConstants.NODESET);
			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				coordinates = currentItem.getTextContent();                                     
			}
		} catch (MalformedURLException ex) {                   
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (XPathExpressionException ex) {
			ex.printStackTrace();
		}

		return coordinates;
	}
}
