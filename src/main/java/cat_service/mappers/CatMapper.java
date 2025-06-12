package cat_service.mappers;

import cat_service.entities.CatEntity;
import cat_service.models.CatModel;
import org.springframework.stereotype.Component;

@Component
public class CatMapper {
  public CatModel toModel(CatEntity entity) {
    var model = new CatModel();
    model.setId(entity.getId());
    model.setName(entity.getName());
    model.setPhotoUrl(entity.getPhotoUrl());
    model.setLikes(entity.getLikedByUsers().size());
    model.setDislikes(entity.getDislikedByUsers().size());
    return model;
  }
}
