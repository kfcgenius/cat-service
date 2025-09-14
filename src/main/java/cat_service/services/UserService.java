package cat_service.services;

import cat_service.entities.UserEntity;
import cat_service.mappers.UserMapper;
import cat_service.models.UserModel;
import cat_service.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public UserModel getOrCreateUser(Long id, String username) {
    var userEntity =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  var newUser = new UserEntity();
                  newUser.setId(id);
                  newUser.setUsername(username);
                  return userRepository.save(newUser);
                });
    return userMapper.toModel(userEntity);
  }
}
