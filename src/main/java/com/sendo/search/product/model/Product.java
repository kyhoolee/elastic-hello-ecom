package com.sendo.search.product.model;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

public class Product {
	private String productId;
	
	private String category;
	private String title;
	private String description;
	private double price;
	
	
	
	public Product() {
		super();
	}
	public Product(String productId, String category, String title,
			String description, double price) {
		super();
		this.productId = productId;
		this.category = category;
		this.title = title;
		this.description = description;
		this.price = price;
	}
	

	
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String toString() {
		String result = "";
		ObjectMapper mapper = JsonFactory.create();
		result = mapper.toJson(this);
		return result;
	}
	

}
