package com.gamq.deportes.repository;

import com.gamq.deportes.model.Pass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassRepository extends JpaRepository<Pass,Long> {

    @Query("SELECT c FROM Pass c WHERE c.uuid = ?1")
    Optional<Pass> findByUuid(String uuid);


}
