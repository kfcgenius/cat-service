package cat_service.kafka;

import cat_service.constants.KafkaAction;
import cat_service.constants.KafkaResponseStatus;
import cat_service.constants.KafkaTopic;
import cat_service.dto.*;
import cat_service.telegram.TelegramMessageDispatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBotMessageConsumer {

  private final TelegramMessageDispatcher dispatcher;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopic.BOT, groupId = KafkaTopic.BOT)
  public void handleResponse(String message) throws JsonProcessingException {
    var request = objectMapper.readValue(message, KafkaBotDto.class);
    if (!request.getStatus().equals(KafkaResponseStatus.SUCCESS)
        || !request.getAction().equals(KafkaAction.GET_RANDOM_CAT)
        || request.getUserId() == null
        || request.getResult() == null) {
      return;
    }

    var catDto = objectMapper.convertValue(request.getResult(), CatDto.class);
    var chatId = request.getUserId();

    var telegramResponse = new TelegramResponseDto();
    telegramResponse.setChatId(chatId);
    telegramResponse.setText("Here's a cat for you: " + catDto.getName());
    telegramResponse.setPhoto(catDto.getPhotoUrl());

    telegramResponse.setButtons(
        List.of(
            new TelegramButtonDto("👍 Like (" + catDto.getLikes() + ")", "like_" + catDto.getId()),
            new TelegramButtonDto(
                "👎 Dislike (" + catDto.getDislikes() + ")", "dislike_" + catDto.getId()),
            new TelegramButtonDto("🏠 Main menu", "main_menu")));

    dispatcher.send(telegramResponse);
  }
}
