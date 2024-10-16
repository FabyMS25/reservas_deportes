package com.gamq.deportes.repository;

import com.gamq.deportes.model.PassResets;
import com.gamq.deportes.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassResetsRepository extends JpaRepository<PassResets,Long> {

    @Query("SELECT c FROM PassResets c WHERE c.uuid = ?1")
    Optional<PassResets> findByUuid(String uuid);


}
