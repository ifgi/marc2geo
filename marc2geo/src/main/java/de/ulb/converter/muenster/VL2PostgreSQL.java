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

public class VL2PostgreSQL {

	private static String vlidFile ="VLIDs.txt";

	public static void main(String[] args) {
		
		System.out.println("Start: " + Instant.now()+"\n");
		
		String[] ids = loadIDs();

		for (int i = 0; i < ids.length; i++) {
			
			insert(ids[i].trim());
			System.out.println("["+i + "] -> Inserting METS XML document for "+ids[i].trim()+" ... ");
		}
		
		System.out.println("\nFinished: " + Instant.now());

	}

	private static void insert(String vlid){


		Connection con = null ;
		PreparedStatement pst = null;

		try {

			
			URL url = new URL("http://sammlungen.ulb.uni-muenster.de/oai/?verb=GetRecord&metadataPrefix=mets&identifier="+vlid);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();           
			Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(url.openStream()));                                   
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();

			/**
			 * HBZID
			 */
			XPathExpression expr = xpath.compile("//mods/identifier[@type='hbz-idn']");
			NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			String hbzid = null;
			
			if(nl.getLength()!=0){
				Node currentItem = nl.item(0);	    
				hbzid = currentItem.getTextContent().trim();
			} else {
				System.err.println("The VL document has no HBZID: " + vlid);
			}
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
			
			String stm = "";
			
			if(hbzid==null){
				
				stm = "INSERT INTO vldocuments (vlid, rawxml) VALUES ("+vlid+",'" + output.replace("'", "") + "')";
				
			}else{
				
				stm = "INSERT INTO vldocuments (vlid, hbzid, rawxml) VALUES ("+vlid+",'"+ hbzid + "','" + output.replace("'", "") + "')";
				
			}
			
			con = DriverManager.getConnection("jdbc:postgresql://ubsvirt136.uni-muenster.de/disco2", "disco2", "Feierabend");
			pst = con.prepareStatement(stm);

			pst.executeUpdate();

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
		} catch (SQLException e) {
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

	private static String[] loadIDs(){

		String content ="";	

		try{

			DataInputStream dis = new DataInputStream (new FileInputStream (vlidFile));

			byte[] datainBytes = new byte[dis.available()];
			dis.readFully(datainBytes);
			dis.close();

			content = new String(datainBytes, 0, datainBytes.length);

		}catch(Exception ex){

			ex.printStackTrace();

		}

		String[] result = content.split(",");

		return result;

	}
}
