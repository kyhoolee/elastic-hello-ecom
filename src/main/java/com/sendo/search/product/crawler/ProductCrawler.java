package com.sendo.search.product.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sendo.search.product.elastic.ElasticProductDAO;
import com.sendo.search.product.model.Product;
import com.sendo.search.product.util.FileUtils;

public class ProductCrawler {

	public static String seedUrl = "https://www.sendo.vn/thoi-trang-nu/";
	private static ElasticProductDAO dao_ = new ElasticProductDAO(); 

	public static Set<String> parseLink(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		// #content > div > div:nth-child(3) > div.block-category-top >
		// div.container > div > div.list-cate-wrap > ul
		Elements links = doc.select("div.block-category-top");
		// System.out.println(links);
		Elements hrefs = links.select("a[href]");
		// System.out.println(hrefs);

		Set<String> catLink = new HashSet<String>();

		for (Element e : hrefs) {
			String l = e.attr("href");
			// System.out.println(l);
			catLink.add(l);
		}

		return catLink;

	}

	public static Set<String> parseProductUrl(String url) throws IOException {
		Set<String> result = new HashSet<String>();
		Document doc = Jsoup.connect(url).get();
		// #box-search > div.responsive-search-product
		Elements products = doc.select("div.box_product a.name_product[href]");

		for (Element p : products) {
			// System.out.println(p);
			String pUrl = p.attr("href");
			result.add(pUrl);
			// System.out.println(pUrl);
		}
		System.out.println(result.size());
		return result;
	}

	public static void scrapProductUrl(String filePath) {
		try {
			Set<String> catLinks = parseLink(seedUrl);
			Set<String> proLinks = new HashSet<String>();
			for (String link : catLinks) {
				// System.out.println("\n\n" + link);
				try {
					proLinks.addAll(parseProductUrl(link));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			FileUtils.writeFile(filePath, proLinks);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Product> scrapProduct(String filePath) {
		List<Product> result = new ArrayList<Product>();
		List<String> products = new ArrayList<String>();
		try {
			List<String> prodUrls = FileUtils.readFile(filePath);
			for (String url : prodUrls) {
				//System.out.println(url);
				Product p = parseProduct(url);
				if(p != null) {
					result.add(p);
					products.add(p.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileUtils.writeFile("data/product_json.txt", products);
		return result;
	}

	public static Product parseProduct(String url) {
		Product result = new Product();

		try {
			Document doc = Jsoup.connect(url).get();
			try {
				String category = doc.select("a.no-p").attr("title");
				result.setCategory(category);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				String description = doc.select("meta[name=description]")
						.attr("content");
				result.setDescription(description);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String price = doc
						.select("meta[property=product:price:amount").attr(
								"content");
				result.setPrice(Double.parseDouble(price));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String productId = doc.select(
						"meta[property=product:retailer_item_id").attr(
						"content");
				result.setProductId(productId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				String title = doc.select("meta[property=og:title").attr(
						"content");
				result.setTitle(title);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}
	
	
	public static Product jsonToProduct(String json) {
		ObjectMapper mapper = JsonFactory.create();
		Product p = mapper.fromJson(json, Product.class);
		
		return p;
	}
	
	public static void insertProductByFile(String filePath) {
		try {
			List<Product> proList = readProduct(filePath);
			dao_.insertProductList(proList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertProductByFile(String filePath, int offset, int numb) {
		try {
			List<Product> proList = readProduct(filePath);
			dao_.insertProductList(proList.subList(offset, offset + numb));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<Product> readProduct(String filePath) {
		List<Product> result = new ArrayList<Product>();
		try {
			List<String> productJson = FileUtils.readFile(filePath);
			
			for(String p: productJson) {
				Product product = jsonToProduct(p);
				result.add(product);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static void main(String[] args) {
		// scrapProductUrl("data/product_url.txt");
		scrapProduct("data/product_url.txt");
	}
}
