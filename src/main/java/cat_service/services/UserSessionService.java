package cat_service.services;

import cat_service.configs.SessionConfig;
import cat_service.entities.UserSessionEntity;
import cat_service.models.UserSessionModel;
import cat_service.repositories.UserSessionRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSessionService {

  private final UserSessionRepository repository;
  private final SessionConfig configs;

  public UserSessionModel get(Long chatId) {
    var sessionEntity =
        repository
            .findById(chatId)
            .orElseGet(
                () -> {
                  var newSessionEntity = new UserSessionEntity();
                  newSessionEntity.setChatId(chatId);
                  return repository.save(newSessionEntity);
                });
    var sessionModel = new UserSessionModel();
    sessionModel.setChatId(chatId);
    sessionModel.setUsername(sessionEntity.getUsername());
    sessionModel.setCatName(sessionEntity.getCatName());
    sessionModel.setCatPhoto(sessionEntity.getCatPhoto());
    sessionModel.setState(sessionEntity.getState());
    return sessionModel;
  }

  public void save(UserSessionModel sessionModel) {
    var sessionEntity = new UserSessionEntity();
    sessionEntity.setChatId(sessionModel.getChatId());
    sessionEntity.setUsername(sessionModel.getUsername());
    sessionEntity.setCatName(sessionModel.getCatName());
    sessionEntity.setCatPhoto(sessionModel.getCatPhoto());
    sessionEntity.setState(sessionModel.getState());
    repository.save(sessionEntity);
  }

  @Scheduled(fixedRate = 3600000)
  public void cleanUpOldSessions() {
    var expiryTime = Instant.now().minus(Duration.ofDays(configs.getTimeoutDays()));
    repository.deleteOldSessions(expiryTime);
  }
}
