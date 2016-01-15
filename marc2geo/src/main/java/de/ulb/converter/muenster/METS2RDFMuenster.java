package de.ulb.converter.muenster;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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

import de.ulb.marc2geo.core.MapRecord;


public class METS2RDFMuenster {
 
	private static String outputFile ="/home/jones/tmp/historicmaps.ttl";
	private static String vlidFile ="/home/jones/tmp/Historische_Karten.csv";
	//private static String vlidFile ="/home/jones/tmp/test.csv";
	private static String lobidBaseURL ="http://lobid.org/resource/";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		METS2RDFMuenster vl = new METS2RDFMuenster();
		String[] ids = vl.loadVLIDs();
		ArrayList<MapRecord> maps = new ArrayList<MapRecord>();
				
		for (int i = 0; i < ids.length; i++) {

			maps.add(vl.parseVLMetadata(ids[i].trim()));
			
		}

		for (int i = 0; i < maps.size(); i++) {
		
			vl.storeRDF(vl.getRDF(maps.get(i)));
			
		}
		
		System.out.println("Total VL Records: " + ids.length);
		System.out.println("Total Maps: " + maps.size());
	}

	private String[] loadVLIDs(){

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

	private MapRecord parseVLMetadata(String vlid) {
    
		MapRecord map = new MapRecord();
		
		System.out.println("VL ID -> " + vlid);
		
		map.setVLID(vlid);
		
		try {

			URL url = new URL("http://sammlungen.ulb.uni-muenster.de/oai/?verb=GetRecord&metadataPrefix=mets&identifier="+vlid);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();           
			org.w3c.dom.Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(url.openStream()));                                   
			XPath xpath = XPathFactory.newInstance().newXPath();                   

			NodeList subfields = (NodeList) xpath.evaluate("//mods/titleInfo/title", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setTitle(currentItem.getTextContent());                                     
			}

			System.out.println("Titel ->  "+map.getTitle());

			
			subfields = (NodeList) xpath.evaluate("//mods/titleInfo/subTitle", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setDescription(currentItem.getTextContent());                                     
			} else {
				
				map.setDescription("");
			}

			System.out.println("Beschreibung ->  "+map.getDescription());
			
			
			subfields = (NodeList) xpath.evaluate("//structMap[@TYPE='LOGICAL']/div/@CONTENTIDS", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setDNB(currentItem.getTextContent());                                     
			}

			System.out.println("DNB -> "+ map.getDNB());



			subfields = (NodeList) xpath.evaluate("//recordInfo/recordIdentifier[@source='ulbmshd']", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setHT(currentItem.getTextContent());  
				
			}

			System.out.println("HT Nummer ->  "+ map.getHT());
			

			subfields = (NodeList) xpath.evaluate("//mods/identifier[@type='hbz-idn']", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setCT(currentItem.getTextContent());                                     
			}

			System.out.println("CT Nummer ->  "+ map.getCT());


//			subfields = (NodeList) xpath.evaluate("//name[@authority='gnd']/@valueURI", document,XPathConstants.NODESET);
//
//			if (subfields.getLength() != 0) {
//
//				String gnd = "";
//				for (int i = 0; i < subfields.getLength(); i++) {
//
//					Node currentItem = subfields.item(i);   
//					if(i>0)gnd = gnd + ",";
//					gnd = gnd + currentItem.getTextContent();
//					
//
//				}
//
//				System.out.println("gnd id ->  "+gnd);
//			}

			subfields = (NodeList) xpath.evaluate("//fileSec/fileGrp[@USE='MAX']/file/FLocat/@href", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setImage(currentItem.getTextContent());                                     
			}

			System.out.println("Image -> "+ map.getImage());


			subfields = (NodeList) xpath.evaluate("//physicalDescription/extent", document,XPathConstants.NODESET);


			if (subfields.getLength() != 0) {

				for (int i = 0; i < subfields.getLength(); i++) {

					Node currentItem = subfields.item(i);   
					String size = currentItem.getTextContent(); 
					map.setScale("");
					map.setMapSize("");
										
					if(size.contains("cm")){

						map.setMapSize(size);

					} else {

						if(size.contains("ca.")){
							
							map.setScale(size);
							
						}
					}

				}

			}
			
			System.out.println("Größe -> " +map.getMapSize());
			System.out.println("Maßstab -> " +map.getScale());

			
			subfields = (NodeList) xpath.evaluate("//links/presentation", document,XPathConstants.NODESET);

			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setPresentation(currentItem.getTextContent());
				map.setURI(currentItem.getTextContent());
			}

			System.out.println("URL -> "+ map.getPresentation());

						
			
			subfields = (NodeList) xpath.evaluate("//header/identifier", document,XPathConstants.NODESET);
								
			
			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setOAI(currentItem.getTextContent());                                     
			}
			
			
			subfields = (NodeList) xpath.evaluate("//originInfo/dateIssued[@keyDate='yes']", document,XPathConstants.NODESET);
								
			
			if (subfields.getLength() != 0) {
				Node currentItem = subfields.item(0);   
				map.setYear(currentItem.getTextContent());                                     
			}
			
			System.out.println("Erscheinungsjahr -> " + map.getYear());
			
			
			
			subfields = (NodeList) xpath.evaluate("//subject/geographic[@authority='gnd']/@valueURI", document,XPathConstants.NODESET);


			
			if (subfields.getLength() != 0) {

				String gnd="";
				
				for (int i = 0; i < subfields.getLength(); i++) {
					
					Node currentItem = subfields.item(i);   
					 

					if(i>0)gnd = gnd + ",";
					gnd = gnd + currentItem.getTextContent();
				}
				
				map.setReferences(gnd);
				System.out.println("GND Ort -> "+ gnd);
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

		System.out.println("");

		return map;
	}

	public String getRDF(MapRecord map){

		String rdfTurtle = "";
 
		rdfTurtle = "\n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.geographicknowledge.de/vocab/maps#Map> . \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#ID> \""+ map.getVLID() +"\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/2002/07/owl#sameAs> <"+ lobidBaseURL+ map.getCT() +">. \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/2002/07/owl#sameAs> <"+ lobidBaseURL+ map.getHT() +">. \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/2002/07/owl#sameAs> <"+ map.getOAI() +">. \n" ;
		
		if(map.getDNB()!=null){
			
			rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.w3.org/2002/07/owl#sameAs> <"+ map.getDNB() +">. \n" ;	
		
		}
			
		if(map.getReferences()!=null){
			
			String[] references =  map.getReferences().split(",");
			
			for (int i = 0; i < references.length; i++) {
				
				rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://purl.org/dc/terms/references> <"+references[i]+"> . \n" ;
				
			}
			
		}
				
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#medium> <http://www.geographicknowledge.de/vocab/maps#Paper> . \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#digitalImageVersion> <" + map.getImage() + ">. \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapSize> \"" + map.getMapSize() + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#title> \"" + map.getTitle().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#presentation> <" + map.getPresentation() + "> . \n" ;				
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#hasScale> \"" + map.getScale() + "\"^^<http://www.w3.org/2001/XMLSchema#string>. \n" ;
		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://purl.org/dc/terms/description> \"" + map.getDescription().replace("'", "\u0027") + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" ;

		rdfTurtle = rdfTurtle + "<" + map.getURI() + "> <http://www.geographicknowledge.de/vocab/maps#mapsTime> _:TIME_" + map.getVLID() + " . \n" ;
		rdfTurtle = rdfTurtle + "_:TIME_" + map.getVLID() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2006/time#Instant> . \n" ;
		rdfTurtle = rdfTurtle + "_:TIME_" + map.getVLID() + " <http://www.w3.org/2001/XMLSchema#gYear> '" + map.getYear() + "'^^<http://www.w3.org/2001/XMLSchema#gYear> .\n" ;

		return rdfTurtle;

	}
			
	public void storeRDF(String sparql){

		try {

			StringBuffer buffer = new StringBuffer();
			buffer.append(sparql);

			FileOutputStream fileStream = new FileOutputStream(new File(outputFile),true);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");

			writer.append(buffer.toString());
			writer.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();		
		}

	}
}
