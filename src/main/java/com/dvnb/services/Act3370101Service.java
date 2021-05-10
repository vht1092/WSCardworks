package com.dvnb.services;

import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.dvnb.entities.Act2110102;
import com.dvnb.entities.Act3370101;

public interface Act3370101Service {

	/**
	 * Danh sach boi user
	 * 
	 * @param page
	 *            PageRequest
	 * @param userid
	 *            user id
	 * @return Page
	 */
	public Page<Act3370101> findAll(Pageable page);
	
	public Page<Act3370101> findAllByKyBaoCao(String kyBaoCao, Pageable page);
	
	public Act3370101 findOneByKyBaoCao(String kyBaoCao);
	
	void create(Act3370101 act3370101);
	
	void deleteByKyBaoCao(String ky);
	
	void deleteByKyBaoCaoAndUserId(String ky, String userId);
}
