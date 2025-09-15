package cat_service.telegram;

import cat_service.models.UserSessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaFacade {

  private final KafkaMessageProducer producer;

  public void sendGetOrCreateUser(Long userId, String username) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.GET_OR_CREATE_USER);
    dto.setUserId(userId);
    dto.setUsername(username);
    producer.sendToServiceTopic(dto);
  }

  public void sendLike(Long userId, String catId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.LIKE_CAT);
    dto.setUserId(userId);
    dto.setCatId(Long.parseLong(catId));
    producer.sendToServiceTopic(dto);
  }

  public void sendDislike(Long userId, String catId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.DISLIKE_CAT);
    dto.setUserId(userId);
    dto.setCatId(Long.parseLong(catId));
    producer.sendToServiceTopic(dto);
  }

  public void sendGetCat(Long userId, String catId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.GET_CAT);
    dto.setUserId(userId);
    dto.setCatId(Long.parseLong(catId));
    producer.sendToServiceTopic(dto);
  }

  public void sendDeleteCat(Long userId, String catId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.DELETE_CAT);
    dto.setUserId(userId);
    dto.setCatId(Long.parseLong(catId));
    producer.sendToServiceTopic(dto);
  }

  public void sendCreateCat(UserSessionModel session) {
    var dto =
        new KafkaServiceDto(
            KafkaAction.CREATE_CAT,
            session.getChatId(),
            session.getUsername(),
            null,
            session.getCatName(),
            session.getCatPhoto());
    producer.sendToServiceTopic(dto);
  }

  public void sendGetRandomCat(Long userId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.GET_RANDOM_CAT);
    dto.setUserId(userId);
    producer.sendToServiceTopic(dto);
  }

  public void sendGetUserCats(Long userId) {
    var dto = new KafkaServiceDto();
    dto.setAction(KafkaAction.GET_USER_CATS);
    dto.setUserId(userId);
    producer.sendToServiceTopic(dto);
  }
}
