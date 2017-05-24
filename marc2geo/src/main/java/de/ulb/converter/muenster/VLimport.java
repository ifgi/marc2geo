package de.ulb.converter.muenster;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VLimport {

	private static String vlidFile ="VLIDs.txt";
	
	public static void main(String[] args) {

		try {
			
			String[] vlids = loadVLIDs();
			
			for (int i = 0; i < vlids.length; i++) {
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new URL("http://sammlungen.ulb.uni-muenster.de/oai?verb=GetRecord&metadataPrefix=mods&identifier="+vlids[i].trim()).openStream());	    
			    			    		
				DOMSource domSource = new DOMSource(doc);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.transform(domSource, result);
				System.out.println(writer.toString());

			}
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	
	}

	
	private static String[] loadVLIDs(){

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
