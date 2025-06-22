package cat_service.repositories;

import cat_service.entities.UserSessionEntity;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM UserSessionEntity s WHERE s.updatedAt < :expiryTime")
  void deleteOldSessions(@Param("expiryTime") Instant expiryTime);
}
