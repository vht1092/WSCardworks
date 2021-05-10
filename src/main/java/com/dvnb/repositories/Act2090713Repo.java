package com.dvnb.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dvnb.entities.Act2090713;

@Repository
public interface Act2090713Repo extends JpaRepository<Act2090713, String> {
	
	

	Page<Act2090713> findAll(Pageable page);
	
	Act2090713 findOneByKy(@Param("ky") String ky);

	void deleteByKy(@Param("ky") String ky);
	
	void deleteByKyAndUsrId(@Param("ky") String ky,@Param("usrId") String usrId);
}
