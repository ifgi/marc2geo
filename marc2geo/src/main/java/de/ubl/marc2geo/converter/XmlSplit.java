package de.ubl.marc2geo.converter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlSplit {

	public static void main(String [] args) throws Exception {

		int records = 850;
		String outputPath="/home/jones/delete/";
		
		System.out.println("Loading XML document with "+ records +" records ..." );
		
		URL url = new URL("http://sru.gbv.de/opac-de-7?version=1.2&operation=searchRetrieve&query=pica.sst%3Dkts+sortby+year%2Fdescending&maximumRecords="+records);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		Document doc = dbf.newDocumentBuilder().parse(new InputSource(url.openStream()));
				
		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList nodes = (NodeList) xpath.evaluate("//records/record", doc, XPathConstants.NODESET);

		System.out.println("Creating SQL INSERT statements ..." );
		
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < nodes.getLength(); i++) {

			StringWriter sw = new StringWriter();
			
			
			try {
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				t.transform(new DOMSource(nodes.item(i)), new StreamResult(sw));
				
				buffer.append("INSERT INTO transfer.goettingen (xml) VALUES ('"+sw.toString().replace("\'", "''")+"');\n");
								
			} catch (TransformerException te) {
				System.out.println("nodeToString Transformer Exception");
			}


		}

		System.out.println("Printing SQL INSERT Statements to file ... ");
		
		PrintWriter writer = new PrintWriter(outputPath+"output.sql", "UTF-8");
		writer.println(buffer.toString());
		writer.close();
		
		System.out.println("Export finished.");
	}

}



