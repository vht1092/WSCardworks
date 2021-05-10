package com.dvnb.services;

import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.dvnb.entities.Act2100501;
import com.dvnb.entities.Act1430101;

public interface Act1430101Service {

	/**
	 * Danh sach boi user
	 * 
	 * @param page
	 *            PageRequest
	 * @param userid
	 *            user id
	 * @return Page
	 */
	public Page<Act1430101> findAll(Pageable page);
	
	public Page<Act1430101> findAllByKyBaoCao(String kyBaoCao, Pageable page);
	
	public Act1430101 findOneByKyBaoCao(String kyBaoCao);
	
	void create(Act1430101 act1430101);
	
	void deleteByKyBaoCao(String ky);
	
	void deleteByKyBaoCaoAndUserId(String ky, String userId);
}
