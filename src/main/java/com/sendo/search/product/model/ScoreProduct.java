package com.sendo.search.product.model;

import java.util.Random;

public class ScoreProduct extends Product {
	private String id;
	private double score;
	private int brand;

	public ScoreProduct() {
		super();
	}
	public ScoreProduct(String id, double score, String productId, String category, String title,
			String description, double price) {
		super(productId, category, title, description, price);
		this.score = score;
		this.id = id;
		Random rand = new Random(System.currentTimeMillis());
		this.brand = Math.abs(rand.nextInt() % 2) + 1;
				
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
	public int getBrand() {
		return brand;
	}
	public void setBrand(int brand) {
		this.brand = brand;
	}
	

}
