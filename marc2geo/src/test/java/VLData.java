import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class VLData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		VLData vl = new VLData();		
		vl.getVLMetadata("HT017212150X");
		
	}

	
	private String getVLMetadata(String HTNumber) {
		
		String result="";         
		
		try {
			
			URL url = new URL("http://sammlungen.ulb.uni-muenster.de/sru?operation=searchRetrieve&query=dc.identifier%3D"+HTNumber);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();           
			org.w3c.dom.Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(url.openStream()));                                   
			XPath xpath = XPathFactory.newInstance().newXPath();                   

			NodeList subfields = (NodeList) xpath.evaluate("//records/record/recordData/mods/physicalDescription/extent", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				result = currentItem.getTextContent();                                     
			}
			
			System.out.println("paper map size ->  "+result);
						
			subfields = (NodeList) xpath.evaluate("//records/record/recordData/mods/identifier[@type='urn']", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				result = currentItem.getTextContent();                                     
			}
			
			System.out.println("URN -> "+result);

		
			subfields = (NodeList) xpath.evaluate("//records/record/recordData/mods/subject/geographic[@authority='gnd']/@valueURI", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				
				for (int i = 0; i < subfields.getLength(); i++) {
				
					Node currentItem = subfields.item(i);   
					result = currentItem.getTextContent();
					System.out.println("gnd id ->  "+result);
					
				}
								                                     
			}
			
			
			
			subfields = (NodeList) xpath.evaluate("//extraRecordData/imgid", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				result = currentItem.getTextContent();                                     
			}
			
			System.out.println("Image -> http://sammlungen.ulb.uni-muenster.de/download/webcache/0/"+result);

			
			
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

		
		return result;
	}
}
