package com.gamq.licencias.repository;

import com.gamq.licencias.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface canchasRepository extends JpaRepository<canchas, Long> {

    @Query("SELECT c FROM canchas c WHERE c.uuid = ?1")
    Optional<canchas> findByUuid(String uuid);


}
