package com.gamq.licencias.repository;

import com.gamq.licencias.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface canchasRepository extends JpaRepository<canchas, Long> {

    @Query("SELECT c FROM canchas c WHERE c.uuid = ?1")
    Optional<canchas> findByUuid(String uuid);

    @Query("SELECT c FROM canchas c WHERE c.complejo.uuid = ?1")
    List<canchas> findCanchasByComplejo(String uuidComplejo);

}
