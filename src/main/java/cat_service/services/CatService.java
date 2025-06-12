package cat_service.services;

import cat_service.entities.CatEntity;
import cat_service.mappers.CatMapper;
import cat_service.models.CatModel;
import cat_service.repositories.CatRepository;
import cat_service.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatService {
  private final CatRepository catRepository;
  private final UserRepository userRepository;
  private final CatMapper catMapper;

  @Transactional
  public CatModel createCat(String name, String photoUrl, Long userId) {
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

    var catEntity = new CatEntity();
    catEntity.setName(name);
    catEntity.setPhotoUrl(photoUrl);
    catEntity.setOwner(user);

    var savedCat = catRepository.save(catEntity);
    return catMapper.toModel(savedCat);
  }

  @Transactional(readOnly = true)
  public List<CatModel> getUserCats(Long userId) {
    return userRepository
        .findById(userId)
        .map(catRepository::findByOwner)
        .orElse(Collections.emptyList())
        .stream()
        .map(catMapper::toModel)
        .toList();
  }

  @Transactional
  public CatModel likeCat(Long catId, Long userId) {
    var userEntity =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    var catEntity =
        catRepository
            .findById(catId)
            .orElseThrow(() -> new EntityNotFoundException("Cat not found with id: " + catId));

    if (userEntity.getLikedCats().contains(catEntity)) {
      return catMapper.toModel(catEntity);
    }

    userEntity.getDislikedCats().remove(catEntity);
    catEntity.getDislikedByUsers().remove(userEntity);

    userEntity.getLikedCats().add(catEntity);
    catEntity.getLikedByUsers().add(userEntity);

    userRepository.save(userEntity);
    catRepository.save(catEntity);

    return catMapper.toModel(catEntity);
  }

  @Transactional
  public CatModel dislikeCat(Long catId, Long userId) {
    var userEntity =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    var catEntity =
        catRepository
            .findById(catId)
            .orElseThrow(() -> new EntityNotFoundException("Cat not found with id: " + catId));

    if (userEntity.getDislikedCats().contains(catEntity)) {
      return catMapper.toModel(catEntity);
    }

    userEntity.getLikedCats().remove(catEntity);
    catEntity.getLikedByUsers().remove(userEntity);

    userEntity.getDislikedCats().add(catEntity);
    catEntity.getDislikedByUsers().add(userEntity);

    userRepository.save(userEntity);
    catRepository.save(catEntity);

    return catMapper.toModel(catEntity);
  }

  @Transactional(readOnly = true)
  public CatModel getRandomUnratedCat(Long userId) {
    return userRepository
        .findById(userId)
        .flatMap(catRepository::findRandomUnratedCatByUser)
        .map(catMapper::toModel)
        .orElseThrow(() -> new EntityNotFoundException("No unrated cat found for user " + userId));
  }

  @Transactional
  public CatModel deleteCat(Long userId, Long catId) {
    var catEntity =
        catRepository
            .findById(catId)
            .filter(cat -> cat.getOwner().getId().equals(userId))
            .orElseThrow(() -> new EntityNotFoundException("Cat not found or not owned by user"));
    catRepository.delete(catEntity);
    return catMapper.toModel(catEntity);
  }
}
