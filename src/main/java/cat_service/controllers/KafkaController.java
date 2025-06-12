package cat_service.controllers;

import cat_service.constants.KafkaAction;
import cat_service.constants.KafkaResponseStatus;
import cat_service.constants.KafkaTopic;
import cat_service.dto.KafkaRequest;
import cat_service.dto.KafkaResponse;
import cat_service.services.CatService;
import cat_service.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaController {

  private final CatService catService;
  private final UserService userService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopic.SERVICE)
  public void handleRequest(@Payload KafkaRequest request) throws JsonProcessingException {
    KafkaResponse response = new KafkaResponse();
    response.setAction(request.getAction());
    response.setUserId(request.getUserId());
    response.setStatus(KafkaResponseStatus.SUCCESS);

    try {
      switch (request.getAction()) {
        case KafkaAction.CREATE_CAT -> {
          var cat =
              catService.createCat(request.getName(), request.getPhotoUrl(), request.getUserId());
          response.setResult(cat);
        }
        case KafkaAction.LIKE_CAT -> {
          var cat = catService.likeCat(request.getCatId(), request.getUserId());
          response.setResult(cat);
        }
        case KafkaAction.DISLIKE_CAT -> {
          var cat = catService.dislikeCat(request.getCatId(), request.getUserId());
          response.setResult(cat);
        }
        case KafkaAction.GET_USER_CATS -> {
          var cats = catService.getUserCats(request.getUserId());
          response.setResult(cats);
        }
        case KafkaAction.DELETE_CAT -> {
          var cat = catService.deleteCat(request.getUserId(), request.getCatId());
          response.setResult(cat);
        }
        case KafkaAction.GET_RANDOM_CAT -> {
          var cat = catService.getRandomUnratedCat(request.getUserId());
          response.setResult(cat);
        }
        case KafkaAction.GET_OR_CREATE_USER -> {
          var user = userService.getOrCreateUser(request.getUserId(), request.getUsername());
          response.setResult(user);
        }
        default -> {
          response.setStatus(KafkaResponseStatus.ERROR);
          response.setError("Unknown action: " + request.getAction());
        }
      }
    } catch (Exception e) {
      response.setStatus(KafkaResponseStatus.ERROR);
      response.setError(e.getMessage());
    }

    var responseJson = objectMapper.writeValueAsString(response);
    kafkaTemplate.send(KafkaTopic.BOT, responseJson);
  }
}
