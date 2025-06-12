package cat_service.mappers;

import cat_service.entities.UserEntity;
import cat_service.models.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserModel toModel(UserEntity entity) {
    var model = new UserModel();
    model.setId(entity.getId());
    model.setUsername(entity.getUsername());
    return model;
  }
}
