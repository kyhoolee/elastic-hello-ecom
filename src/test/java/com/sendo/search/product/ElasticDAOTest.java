package com.sendo.search.product;

import java.io.IOException;
import java.util.List;

import com.sendo.search.product.crawler.ProductCrawler;
import com.sendo.search.product.elastic.ElasticProductDAO;
import com.sendo.search.product.model.Product;
import com.sendo.search.product.model.ScoreProduct;

public class ElasticDAOTest {

	public static void main(String[] args) {
		// deleteProduct();
		//insertProduct();
		// insertProductByFile();
		// System.out.println("---");
		getProduct();
		// System.out.println("\u00a5123");
		//updateSynonym();
		//searchProduct();
		// getProduct("6555126");
		// updateProduct("6555126");
	}
	
	public static void updateSynonym() {
		ElasticProductDAO elastic = new ElasticProductDAO();
		try {
			elastic.setSynonym();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public static void updateProduct(String id) {
		// 6555126
		ElasticProductDAO elastic = new ElasticProductDAO();
		ScoreProduct p = elastic.getScoreProduct(id);

		// System.out.println("--------------");
		System.out.println(p.getProductId());
		System.out.println(p.getTitle());
		// System.out.println(p.getDescription());

		String newTitle = p.getTitle().replace(" " + p.getProductId(), "");
		p.setTitle(newTitle);
		System.out.println(p.getTitle());
		System.out.println(p);
		elastic.updateProduct(p);
		
		p = elastic.getScoreProduct(id);
		System.out.println(p.getTitle());
	}

	public static void getProduct(String id) {
		// 6555126
		ElasticProductDAO elastic = new ElasticProductDAO();
		ScoreProduct p = elastic.getScoreProduct(id);

		// System.out.println("--------------");
		System.out.println(p.getProductId());
		System.out.println(p.getTitle());
		// System.out.println(p.getDescription());

	}

	public static void searchProduct() {
		ElasticProductDAO elastic = new ElasticProductDAO();
		List<ScoreProduct> ps = elastic.searchProductTitle("đầm", 0, 30).getProList();
		System.out.println(ps.size());
		for (ScoreProduct p : ps) {
			System.out.println("--------------");
			System.out.println(p.getProductId());
			System.out.println(p.getTitle());
			System.out.println(p.getScore());
			System.out.println(p.getPrice());
			System.out.println(p.getDescription());
		}
	}

	public static void getProduct() {
		ElasticProductDAO elastic = new ElasticProductDAO();
		List<ScoreProduct> ps = elastic.getAllScoreProduct();
		System.out.println(ps.size());
		for (Product p : ps) {
			// System.out.println("--------------");
			System.out.println(p.getProductId());
			//System.out.println(p.getTitle());
			// System.out.println(p.getDescription());
			String newTitle = p.getTitle().replace(" " + p.getProductId(), "");
			p.setTitle(newTitle);
			//System.out.println(p.getTitle());
			elastic.updateProduct(p);
			
		}
	}

	public static void insertProduct() {
		Product p = new Product("123", "thời trang", "áo thun hè",
				"áo hè siêu đẹp, siêu mát", 120000);
		ElasticProductDAO elastic = new ElasticProductDAO();
		elastic.insertProduct(p);
	}

	public static void deleteProduct(String productId) {
		ElasticProductDAO elastic = new ElasticProductDAO();
		elastic.deleteProduct(productId);
	}

	public static void deleteAllProduct() {
		ElasticProductDAO elastic = new ElasticProductDAO();
	}

	public static void insertProductByFile() {
		ProductCrawler.insertProductByFile("data/product_json.txt");
	}

}
