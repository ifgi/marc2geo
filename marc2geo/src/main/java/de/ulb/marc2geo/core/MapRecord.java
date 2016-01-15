package de.ulb.marc2geo.core;


public class MapRecord {

	private String uri;
	private String vlid;
	private String oai;
	private String htid;
	private String ctid;
	private String dnbid;
	private String title;
	private String size;
	private String geometry;
	private String image;
	private String presentation;
	private String year;
	private String description;
	private String scale;
	private String references;

	public MapRecord() {
		
		super();
		
	}
	
	
	
	
	
	public String getReferences() {
		return references;
	}





	public void setReferences(String references) {
		this.references = references;
	}





	public String getOAI() {
		return oai;
	}





	public void setOAI(String oai) {
		this.oai = oai;
	}





	public String getVLID() {
		return vlid;
	}



	public void setVLID(String vlid) {
		this.vlid = vlid;
	}



	public String getCT() {
		return ctid;
	}



	public void setCT(String ctid) {
		this.ctid = ctid;
	}



	public String getDNB() {
		return dnbid;
	}



	public void setDNB(String dnbid) {
		this.dnbid = dnbid;
	}



	public String getPresentation() {
		return presentation;
	}


	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}


	public String getURI() {
		return uri;
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getMapSize() {
		return size;
	}
	
	public void setMapSize(String scale) {
		this.size = scale;
	}
	
	public String getGeometry() {
		return geometry;
	}
	
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHT() {
		
		return htid;
		
	}

	public void setHT(String ht) {
	
		this.htid = ht;
		
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}
	
	

}
