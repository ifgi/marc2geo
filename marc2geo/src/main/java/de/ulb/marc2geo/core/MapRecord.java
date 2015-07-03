package de.ulb.marc2geo.core;

public class MapRecord {

	private String uri;
	private String id;
	private String title;
	private String size;
	private String geometry;
	private String image;
	private String year;
	private String description;
	private String scale;

	public MapRecord() {
		
		super();
		
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
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

	public String getId() {
		
		return id;
		
	}

	public void setId(String id) {
	
		this.id = id;
		
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}
	
	

}
