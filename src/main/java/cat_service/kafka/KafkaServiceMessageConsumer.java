package cat_service.kafka;

import cat_service.constants.KafkaAction;
import cat_service.constants.KafkaResponseStatus;
import cat_service.constants.KafkaTopic;
import cat_service.dto.*;
import cat_service.mappers.CatMapper;
import cat_service.services.CatService;
import cat_service.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaServiceMessageConsumer {

  private final CatService catService;
  private final UserService userService;
  private final CatMapper catMapper;
  private final KafkaMessageProducer kafkaMessageProducer;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopic.SERVICE, groupId = KafkaTopic.SERVICE)
  public void handleRequest(String message) throws JsonProcessingException {
    var request = objectMapper.readValue(message, KafkaServiceDto.class);
    switch (request.getAction()) {
      case KafkaAction.CREATE_CAT ->
          catService.createCat(request.getName(), request.getPhotoUrl(), request.getUserId());
      case KafkaAction.LIKE_CAT -> catService.likeCat(request.getCatId(), request.getUserId());
      case KafkaAction.DISLIKE_CAT ->
          catService.dislikeCat(request.getCatId(), request.getUserId());
      case KafkaAction.GET_USER_CATS -> {
        var catModels = catService.getUserCats(request.getUserId());
        var catDtos = catModels.stream().map(catMapper::toDto).toList();
        var kafkaResponseDto =
            new KafkaBotDto(
                KafkaResponseStatus.SUCCESS,
                request.getAction(),
                request.getUserId(),
                null,
                catDtos);
        kafkaMessageProducer.sendToBot(kafkaResponseDto);
      }
      case KafkaAction.DELETE_CAT -> catService.deleteCat(request.getUserId(), request.getCatId());
      case KafkaAction.GET_RANDOM_CAT -> {
        var catModel = catService.getRandomUnratedCat(request.getUserId());
        var catDto = catMapper.toDto(catModel);
        var kafkaResponseDto =
            new KafkaBotDto(
                KafkaResponseStatus.SUCCESS,
                request.getAction(),
                request.getUserId(),
                null,
                catDto);
        kafkaMessageProducer.sendToBot(kafkaResponseDto);
      }
      case KafkaAction.GET_OR_CREATE_USER ->
          userService.getOrCreateUser(request.getUserId(), request.getUsername());
    }
  }
}
