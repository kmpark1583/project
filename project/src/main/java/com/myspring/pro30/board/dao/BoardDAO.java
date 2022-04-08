package com.myspring.pro30.board.dao;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.myspring.pro30.board.vo.ArticleVO;


public interface BoardDAO {
	public List selectAllArticlesList() throws DataAccessException;
}
