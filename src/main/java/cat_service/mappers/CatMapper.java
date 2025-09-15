package cat_service.mappers;

import cat_service.dto.CatDto;
import cat_service.entities.CatEntity;
import cat_service.models.CatModel;
import org.springframework.stereotype.Component;

@Component
public class CatMapper {

  public CatModel toModel(CatEntity entity) {
    var model = new CatModel();
    model.setId(entity.getId());
    model.setName(entity.getName());
    model.setOwner(entity.getOwner().getUsername());
    model.setPhoto(entity.getCatPhoto());
    model.setLikes(entity.getLikedByUsers().size());
    model.setDislikes(entity.getDislikedByUsers().size());
    return model;
  }

  public CatDto toDto(CatModel model) {
    var dto = new CatDto();
    dto.setId(model.getId());
    dto.setName(model.getName());
    dto.setOwner(model.getOwner());
    dto.setPhoto(model.getPhoto());
    dto.setLikes(model.getLikes());
    dto.setDislikes(model.getDislikes());
    return dto;
  }
}
