package de.ulb.marc2geo.core;

import java.util.Date;

public class MapRecord {

	private String uri;
	private String title;
	private String scale;
	private String geometry;
	private String image;
	private Date year;
	private String description;

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
	
	public String getScale() {
		return scale;
	}
	
	public void setScale(String scale) {
		this.scale = scale;
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
	
	public Date getYear() {
		return year;
	}
	
	public void setYear(Date year) {
		this.year = year;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}



}
