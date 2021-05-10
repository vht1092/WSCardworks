package com.dvnb.services;

import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.dvnb.entities.Act2100502;
import com.dvnb.entities.Act2100503;

public interface Act2100503Service {

	/**
	 * Danh sach boi user
	 * 
	 * @param page
	 *            PageRequest
	 * @param userid
	 *            user id
	 * @return Page
	 */
	public Page<Act2100503> findAll(Pageable page);
	
	public Page<Act2100503> findAllByKyBaoCao(String kyBaoCao, Pageable page);
	
	public Act2100503 findOneByKyBaoCao(String kyBaoCao);
	
	void create(Act2100503 act2100503);
	
	void deleteByKyBaoCao(String ky);
	
	void deleteByKyBaoCaoAndUserId(String ky, String userId);
}
