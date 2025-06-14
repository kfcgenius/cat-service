package cat_service.kafka;

import cat_service.constants.KafkaAction;
import cat_service.constants.KafkaResponseStatus;
import cat_service.constants.KafkaTopic;
import cat_service.dto.KafkaRequestDto;
import cat_service.dto.KafkaResponseDto;
import cat_service.mappers.CatMapper;
import cat_service.mappers.UserMapper;
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
public class KafkaMessageConsumer {

  private final CatService catService;
  private final UserService userService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final CatMapper catMapper;
  private final UserMapper userMapper;

  @KafkaListener(topics = KafkaTopic.SERVICE)
  public void handleRequest(@Payload KafkaRequestDto request) throws JsonProcessingException {
    String responseJson;

    try {
      switch (request.getAction()) {
        case KafkaAction.CREATE_CAT -> {
          var catModel =
              catService.createCat(request.getName(), request.getPhotoUrl(), request.getUserId());
          var catDto = catMapper.toDto(catModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.LIKE_CAT -> {
          var catModel = catService.likeCat(request.getCatId(), request.getUserId());
          var catDto = catMapper.toDto(catModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.DISLIKE_CAT -> {
          var catModel = catService.dislikeCat(request.getCatId(), request.getUserId());
          var catDto = catMapper.toDto(catModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.GET_USER_CATS -> {
          var catModels = catService.getUserCats(request.getUserId());
          var catDtos = catModels.stream().map(catMapper::toDto).toList();
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDtos);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.DELETE_CAT -> {
          var catModel = catService.deleteCat(request.getUserId(), request.getCatId());
          var catDto = catMapper.toDto(catModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.GET_RANDOM_CAT -> {
          var catModel = catService.getRandomUnratedCat(request.getUserId());
          var catDto = catMapper.toDto(catModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  catDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        case KafkaAction.GET_OR_CREATE_USER -> {
          var userModel = userService.getOrCreateUser(request.getUserId(), request.getUsername());
          var userDto = userMapper.toDto(userModel);
          var kafkaResponseDto =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.SUCCESS,
                  request.getAction(),
                  request.getUserId(),
                  null,
                  userDto);
          responseJson = objectMapper.writeValueAsString(kafkaResponseDto);
        }
        default -> {
          var response =
              new KafkaResponseDto<>(
                  KafkaResponseStatus.ERROR,
                  request.getAction(),
                  request.getUserId(),
                  "Unknown action: " + request.getAction(),
                  null);
          responseJson = objectMapper.writeValueAsString(response);
        }
      }
    } catch (Exception e) {
      var response =
          new KafkaResponseDto<>(
              KafkaResponseStatus.ERROR,
              request.getAction(),
              request.getUserId(),
              e.getMessage(),
              null);
      responseJson = objectMapper.writeValueAsString(response);
    }

    kafkaTemplate.send(KafkaTopic.BOT, responseJson);
  }
}
