package cat_service.repositories;

import cat_service.entities.CatEntity;
import cat_service.entities.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CatRepository extends JpaRepository<CatEntity, Long> {
  List<CatEntity> findByOwner(UserEntity owner);

  @Query(
      "SELECT c FROM CatEntity c WHERE c.owner = :owner AND c.id NOT IN "
          + "(SELECT lc.id FROM UserEntity u JOIN u.likedCats lc WHERE u = :owner) "
          + "AND c.id NOT IN (SELECT dc.id FROM UserEntity u JOIN u.dislikedCats dc WHERE u = :owner) "
          + "ORDER BY RANDOM() LIMIT 1")
  Optional<CatEntity> findRandomUnratedCatByUser(@Param("owner") UserEntity owner);
}
