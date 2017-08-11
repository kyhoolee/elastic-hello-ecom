package com.sendo.search.product.elastic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

import com.sendo.search.product.Constant;
import com.sendo.search.product.model.ProSearchResult;
import com.sendo.search.product.model.Product;
import com.sendo.search.product.model.ScoreProduct;

public class ElasticProductDAO {

	public ElasticProductDAO() {
		// Init client
		getElasticClient();
	}

	protected static ObjectMapper mapper = JsonFactory.create();
	private static Client client = null;

	private static Client getElasticClient() {
		if (client != null) {
			return client;
		}
		try {
			client = TransportClient
					.builder()
					.build()
					.addTransportAddress(
							new InetSocketTransportAddress(InetAddress
									.getByName(Constant.elastic_host),
									Constant.elastic_port));
			return client;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return client;
	}

	private Product hitToProduct(SearchHit h) {
		Product p = new Product();

		try {
			if (!h.isSourceEmpty()) {
				p = new Product(h.getSource().get("productId").toString(), h
						.getSource().get("category").toString(), h.getSource()
						.get("title").toString(), h.getSource()
						.get("description").toString(), Double.parseDouble(h
						.getSource().get("price").toString()));
			}
		} catch (Exception e) {

		}

		return p;
	}

	private ScoreProduct hitToScoreProduct(SearchHit h) {
		ScoreProduct p = new ScoreProduct();

		try {
			if (!h.isSourceEmpty()) {
				p = new ScoreProduct(h.getId(), h.getScore(), h.getSource()
						.get("productId").toString(), h.getSource()
						.get("category").toString(), h.getSource().get("title")
						.toString(), h.getSource().get("description")
						.toString(), Double.parseDouble(h.getSource()
						.get("price").toString()));
			}
		} catch (Exception e) {

		}

		return p;
	}

	private List<Product> hitToProduct(SearchHit[] hits) {
		List<Product> result = new ArrayList<Product>();
		for (SearchHit h : hits) {
			result.add(hitToProduct(h));
		}
		return result;
	}

	private List<ScoreProduct> hitToScoreProduct(SearchHit[] hits) {
		List<ScoreProduct> result = new ArrayList<ScoreProduct>();
		for (SearchHit h : hits) {
			result.add(hitToScoreProduct(h));
		}
		return result;
	}

	public void insertProductList(List<Product> entities) {
		if (entities == null || entities.size() == 0) {
			return;
		}
		BulkRequestBuilder bulkRequestBuilder = getElasticClient()
				.prepareBulk().setRefresh(true);
		for (Product data : entities) {
			String json = mapper.toJson(data);
			bulkRequestBuilder.add(getElasticClient().prepareIndex(
					Constant.index, Constant.product_type, data.getProductId())
					.setSource(json));
		}
		bulkRequestBuilder.execute().actionGet();
	}

	// public void insertJsonList(List<String> entities) {
	// if (entities == null || entities.size() == 0) {
	// return;
	// }
	// BulkRequestBuilder bulkRequestBuilder =
	// getElasticClient().prepareBulk().setRefresh(true);
	// for (String data : entities) {
	// String json = data;
	// bulkRequestBuilder.add(getElasticClient().prepareIndex(Constant.index,
	// Constant.product_type).setSource(json));
	// }
	// bulkRequestBuilder.execute().actionGet();
	// }

	public IndexResponse insertProduct(Product p) {
		String jsonData = mapper.toJson(p);
		System.out.println(jsonData);
		IndexRequest indexRequest = new IndexRequest(Constant.index,
				Constant.product_type, p.getProductId());
		indexRequest.source(jsonData);
		Client c = getElasticClient();
		if (c != null) {
			IndexResponse response = c.index(indexRequest).actionGet();
			System.out.println(response);
			return response;
		}
		return null;
	}

	public void upsertProduct(Product p) {
		IndexRequest indexRequest = new IndexRequest(Constant.index,
				Constant.product_type, p.getProductId()).source(mapper
				.toJson(p));
		UpdateRequest updateRequest = new UpdateRequest(Constant.index,
				Constant.product_type, p.getProductId()).doc(mapper.toJson(p))
				.upsert(indexRequest);
		try {
			UpdateResponse r = client.update(updateRequest).get();
			// System.out.println(r.getGetResult());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void updateProduct(Product p) {
		UpdateRequest updateRequest = new UpdateRequest(Constant.index,
				Constant.product_type, p.getProductId()).doc(mapper.toJson(p));
		try {
			UpdateResponse r = client.update(updateRequest).get();
			// System.out.println(r.getIndex());
			// System.out.println(r.getType());
			// System.out.println(r.getVersion());
			// System.out.println(r.getGetResult());

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public List<Product> getProduct(int offset, int numb) {
		List<Product> result = new ArrayList<Product>();

		SearchResponse searchResponse = client.prepareSearch(Constant.index)
				.setTypes(Constant.product_type).setSize(numb).setFrom(offset)
				.execute().actionGet();
		SearchHit[] hits = searchResponse.getHits().getHits();

		result = hitToProduct(hits);

		return result;
	}

	public List<Product> getAllProduct() {
		int scrollSize = 1000;
		List<Product> prodList = new ArrayList<Product>();
		SearchResponse response = null;
		int i = 0;
		while (response == null || response.getHits().hits().length != 0) {
			response = client.prepareSearch(Constant.index)
					.setTypes(Constant.product_type)
					.setQuery(QueryBuilders.matchAllQuery())
					.setSize(scrollSize).setFrom(i * scrollSize).execute()
					.actionGet();
			SearchHit[] hits = response.getHits().getHits();

			prodList.addAll(hitToProduct(hits));
			i++;
		}
		return prodList;
	}

	public List<ScoreProduct> getScoreProduct(int offset, int numb) {
		List<ScoreProduct> result = new ArrayList<ScoreProduct>();

		SearchResponse searchResponse = client.prepareSearch(Constant.index)
				.setTypes(Constant.product_type).setSize(numb).setFrom(offset)
				.execute().actionGet();
		SearchHit[] hits = searchResponse.getHits().getHits();

		result = hitToScoreProduct(hits);

		return result;
	}

	public List<ScoreProduct> getAllScoreProduct() {
		int scrollSize = 1000;
		List<ScoreProduct> prodList = new ArrayList<ScoreProduct>();
		SearchResponse response = null;
		int i = 0;
		while (response == null || response.getHits().hits().length != 0) {
			response = client.prepareSearch(Constant.index)
					.setTypes(Constant.product_type)
					.setQuery(QueryBuilders.matchAllQuery())
					.setSize(scrollSize).setFrom(i * scrollSize).execute()
					.actionGet();
			SearchHit[] hits = response.getHits().getHits();

			prodList.addAll(hitToScoreProduct(hits));
			i++;
		}
		return prodList;
	}

	public ScoreProduct getScoreProduct(String productId) {
		ScoreProduct p = new ScoreProduct();

		GetResponse response = client.prepareGet(Constant.index,
				Constant.product_type, productId).get();

		p = mapper.fromJson(response.getSourceAsString(), ScoreProduct.class);

		return p;
	}

	@SuppressWarnings("deprecation")
	public ProSearchResult searchProduct(String query, int offset, int numb) {
		List<ScoreProduct> proList = new ArrayList<ScoreProduct>();

		SearchRequestBuilder srb = client.prepareSearch(Constant.index)
				.setTypes(Constant.product_type)
				.setSize(numb).setFrom(offset);

		QueryBuilder titleQuery = null;
		if (query != null && query.trim().length() > 0) {
			titleQuery = QueryBuilders.matchQuery("title", query);
		}
		QueryBuilder descQuery = null;
		if (query != null && query.trim().length() > 0) {
			descQuery = QueryBuilders.matchQuery("description", query);
		}

		if (titleQuery != null && descQuery == null) {
			srb.setQuery(titleQuery);
		} else if (titleQuery == null && descQuery != null) {
			srb.setQuery(descQuery);
		} else if (titleQuery != null && descQuery != null) {
			srb.setQuery(QueryBuilders.orQuery(descQuery, titleQuery));
		}

		SearchResponse searchResponse = srb.execute().actionGet();

		SearchHit[] hits = searchResponse.getHits().getHits();
		proList = hitToScoreProduct(hits);

		ProSearchResult result = new ProSearchResult(proList);

		return result;

	}

	public ProSearchResult searchProductTitle(String query, int offset, int numb) {
		List<ScoreProduct> proList = new ArrayList<ScoreProduct>();

		SearchRequestBuilder srb = client.prepareSearch(Constant.index)
				.setTypes(Constant.product_type)
				.setSize(numb).setFrom(offset);

		QueryBuilder titleQuery = null;
		if (query != null && query.trim().length() > 0) {
			titleQuery = QueryBuilders.matchQuery("title", query);
		}
		srb.setQuery(titleQuery);

		SearchResponse searchResponse = srb.execute().actionGet();

		SearchHit[] hits = searchResponse.getHits().getHits();
		proList = hitToScoreProduct(hits);

		ProSearchResult result = new ProSearchResult(proList);

		return result;

	}

	public ProSearchResult searchProductDescription(String query, int offset, int numb) {
		List<ScoreProduct> proList = new ArrayList<ScoreProduct>();

		SearchRequestBuilder srb = client.prepareSearch(Constant.index)
				.setTypes(Constant.product_type)
				.setSize(numb).setFrom(offset);

		QueryBuilder descQuery = null;
		if (query != null && query.trim().length() > 0) {
			descQuery = QueryBuilders.matchQuery("description", query);
		}
		srb.setQuery(descQuery);

		SearchResponse searchResponse = srb.execute().actionGet();

		SearchHit[] hits = searchResponse.getHits().getHits();
		proList = hitToScoreProduct(hits);

		ProSearchResult result = new ProSearchResult(proList);

		return result;

	}

	public void deleteProduct(String productId) {
		// DeleteResponse response = client.prepareDelete("twitter", "tweet",
		// "1").get();
		client.prepareDelete(Constant.index, Constant.product_type, productId)
				.get();
	}

	public void setSynonym() throws IOException {
		Settings settings = Settings.settingsBuilder().loadFromSource(
			jsonBuilder()
				.startObject()
					// Add analyzer settings
					.startObject("analysis")
						.startObject("filter")
//							.startObject("test_filter_stopwords_en")
//								.field("type", "stop")
//								.field("stopwords_path", "stopwords/stop_en")
//							.endObject()
//							.startObject("test_filter_snowball_en")
//								.field("type", "snowball")
//								.field("language", "English")
//							.endObject()
//							.startObject("test_filter_worddelimiter_en")
//								.field("type", "word_delimiter")
//								.field("protected_words_path", "worddelimiters/protectedwords_en")
//								.field("type_table_path", "typetable")
//							.endObject()
							.startObject("test_filter_synonyms_vi")
								.field("type", "synonym")
								.field("synonyms_path", "data/synonym/synonym_vi.txt")
								.field("ignore_case", true)
								.field("expand", true)
							.endObject()
//							.startObject("test_filter_ngram")
//								.field("type", "edgeNGram")
//								.field("min_gram", 2)
//								.field("max_gram", 30)
//							.endObject()
						.endObject()
						.startObject("analyzer")
							.startObject("test_analyzer")
								.field("type", "custom")
								.field("tokenizer", "whitespace")
								.field("filter",
										new String[] { "lowercase",
//											"test_filter_worddelimiter_en",
//											"test_filter_stopwords_en",
											"test_filter_synonyms_vi",
//											"test_filter_snowball_en" 
											})
								.field("char_filter", "html_strip")
							.endObject()
						.endObject()
					.endObject()
				.endObject()
				.string()).build();

		CreateIndexRequestBuilder createIndexRequestBuilder = client.admin()
				.indices().prepareCreate(Constant.index);
		createIndexRequestBuilder.setSettings(settings);
	}

}
