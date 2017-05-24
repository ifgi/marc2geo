package de.ulb.converter.muenster;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
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
import org.xml.sax.SAXException;

public class MIAMI2PostgreSQL {

	public static void main(String[] args) {

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL("http://repositorium.uni-muenster.de/oai/beneluxdok?verb=ListRecords&metadataPrefix=mets&from=2016-01-01T00:00:00Z").openStream());	    

			DOMSource domSource = new DOMSource(doc);

			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			
			System.out.println(writer.toString());

			
//			XPathFactory xPathfactory = XPathFactory.newInstance();
//			XPath xpath = xPathfactory.newXPath();
//
//			XPathExpression expr = xpath.compile("//OAI-PMH/ListRecords/record");
//			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
//
//			
//			
//			for (int i = 0; i < nl.getLength(); i++) {
//
//				Node rootNode = nl.item(i);
//
//				System.out.println(rootNode.getTextContent());
//
//				StringWriter sw = new StringWriter();
//
//				Transformer t = TransformerFactory.newInstance().newTransformer();
//				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//				t.transform(new DOMSource(rootNode), new StreamResult(sw));
//				System.out.println(sw.toString());
//
//
//
//		}

			 




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
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}



}
