package com.dvnb.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dvnb.entities.Act2100501;
import com.dvnb.entities.Act1440101;

@Repository
public interface Act1440101Repo extends JpaRepository<Act1440101, String> {
	
	

	Page<Act1440101> findAll(Pageable page);
	
	Page<Act1440101> findAllByKy(@Param("ky") String ky, Pageable page);
	
	Act1440101 findOneByKy(@Param("ky") String ky);
	
	void deleteByKy(@Param("ky") String ky);
	
	void deleteByKyAndUsrId(@Param("ky") String ky,@Param("usrId") String usrId);
	
}
