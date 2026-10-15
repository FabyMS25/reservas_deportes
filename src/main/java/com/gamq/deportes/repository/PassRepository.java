package com.gamq.deportes.repository;

import com.gamq.deportes.model.Pass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.List;
import javax.persistence.LockModeType;

@Repository
public interface PassRepository extends JpaRepository<Pass,Long> {

    @Query("SELECT c FROM Pass c WHERE c.uuid = ?1")
    Optional<Pass> findByUuid(String uuid);

    @Query("SELECT c FROM Pass c WHERE c.token = ?1")
    Optional<Pass> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Pass c WHERE c.token = ?1")
    Optional<Pass> findByTokenForUpdate(String token);

    List<Pass> findAllByEmailIgnoreCase(String email);

}
