package com.cun.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cun.dao.BookDao;
import com.cun.entity.Book;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@RestController
@RequestMapping("/book")
@EnableSwagger2
public class BookController {

	@Autowired
	private BookDao bookDao;

	/**
	 * 1、查  id
	 * @param id
	 * @return
	 */
	@GetMapping("/get/{id}")
	public Book getBookById(@PathVariable String id) {
		return bookDao.findOne(id);
	}

	/**
	 * 2、查  ++:全文检索（根据整个实体的所有属性，可能结果为0个）
	 * @param q
	 * @return
	 */
	@GetMapping("/select/{q}")
	public List<Book> testSearch(@PathVariable String q) {
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(q);
		Iterable<Book> searchResult = bookDao.search(builder);
		Iterator<Book> iterator = searchResult.iterator();
		List<Book> list = new ArrayList<Book>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}

	/**
	 * 3、查   +++：分页、分数、分域（结果一个也不少）
	 * @param page
	 * @param size
	 * @param q
	 * @return 
	 * @return
	 */
	@GetMapping("/{page}/{size}/{q}")
	public List<Book> searchCity(@PathVariable Integer page, @PathVariable Integer size, @PathVariable String q) {

		// 分页参数
		Pageable pageable = new PageRequest(page, size);

		// 分数，并自动按分排序
		FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
				.add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("name", q)),
						ScoreFunctionBuilders.weightFactorFunction(1000)) // 权重：name 1000分
				.add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("message", q)),
						ScoreFunctionBuilders.weightFactorFunction(100)); // 权重：message 100分

		// 分数、分页
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(pageable)
				.withQuery(functionScoreQueryBuilder).build();

		Page<Book> searchPageResults = bookDao.search(searchQuery);
		return searchPageResults.getContent();

	}

	/**
	 * 4、增
	 * @param book
	 * @return
	 */
	@PostMapping("/insert")
	public Book insertBook(Book book) {
		bookDao.save(book);
		return book;
	}

	/**
	 * 5、删 id
	 * @param id
	 * @return
	 */
	@DeleteMapping("/delete/{id}")
	public Book insertBook(@PathVariable String id) {
		Book book = bookDao.findOne(id);
		bookDao.delete(id);
		return book;
	}

	/**
	 * 6、改
	 * @param book
	 * @return
	 */
	@PutMapping("/update")
	public Book updateBook(Book book) {
		bookDao.save(book);
		return book;
	}

}
