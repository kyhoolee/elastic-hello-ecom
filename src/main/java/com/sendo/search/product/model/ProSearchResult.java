package com.sendo.search.product.model;

import java.util.List;

public class ProSearchResult {
	
	
	private List<ScoreProduct> proList;
	
	

	public ProSearchResult() {
		super();
	}

	public ProSearchResult(List<ScoreProduct> proList) {
		super();
		this.proList = proList;
	}

	public List<ScoreProduct> getProList() {
		return proList;
	}

	public void setProList(List<ScoreProduct> proList) {
		this.proList = proList;
	}
	
	

}
