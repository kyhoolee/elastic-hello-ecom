package com.sendo.search.product.model;

public class ScoreProduct extends Product {
	private String id;
	private double score;

	public ScoreProduct() {
		super();
	}
	public ScoreProduct(String id, double score, String productId, String category, String title,
			String description, double price) {
		super(productId, category, title, description, price);
		this.score = score;
		this.id = id;
				
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	

}
