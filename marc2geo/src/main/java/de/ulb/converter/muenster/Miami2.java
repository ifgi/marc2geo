package de.ulb.converter.muenster;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

public class Miami2 {

	

	public static void main(String[] args) {
		


		Connection con = null ;
		PreparedStatement pst = null;

		try {

			
			URL url = new URL("http://repositorium.uni-muenster.de/oai/beneluxdok?verb=ListRecords&metadataPrefix=mets&from=2015-01-01T00:00:00Z");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();           
			Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(url.openStream()));                                   
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();


			XPathExpression expr = xpath.compile("//ListRecords/record/header/identifier");
			NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);


			
			for (int i = 0; i < nl.getLength(); i++) {
				
				System.out.println(nl.item(i).getTextContent());
				
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(document), new StreamResult(writer));
				String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
				
			}	
			
			
			
			
			
			String stm = "";
			
//			if(hbzid==null){
//				
//				stm = "INSERT INTO vldocuments (vlid, rawxml) VALUES ("+vlid+",'" + output.replace("'", "") + "')";
//				
//			}else{
//				
//				stm = "INSERT INTO vldocuments (vlid, hbzid, rawxml) VALUES ("+vlid+",'"+ hbzid + "','" + output.replace("'", "") + "')";
//				
//			}
//			
//			con = DriverManager.getConnection("jdbc:postgresql://ubsvirt136.uni-muenster.de/disco2", "disco2", "Feierabend");
//			pst = con.prepareStatement(stm);
//
//			pst.executeUpdate();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} finally {

	        try {
	            if (pst != null) {
	                pst.close();
	            }
	            if (con != null) {
	                con.close();
	            }

	        } catch (SQLException ex) {
	            Logger lgr = Logger.getLogger(PreparedStatement.class.getName());
	            lgr.log(Level.SEVERE, ex.getMessage(), ex);
	        }

		}

		

	}



	
}
