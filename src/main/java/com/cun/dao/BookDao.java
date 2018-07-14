package com.cun.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cun.entity.Book;

public interface BookDao extends ElasticsearchRepository<Book,String>{

}
