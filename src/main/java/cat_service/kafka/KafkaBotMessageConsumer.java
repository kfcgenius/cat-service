package cat_service.kafka;

import cat_service.constants.KafkaAction;
import cat_service.constants.KafkaResponseStatus;
import cat_service.constants.KafkaTopic;
import cat_service.dto.*;
import cat_service.telegram.TelegramMessageDispatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
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
        || request.getUserId() == null
        || request.getResult() == null) {
      return;
    }

    var chatId = request.getUserId();
    var telegramResponse = new TelegramResponseDto();
    telegramResponse.setChatId(chatId);

    switch (request.getAction()) {
      case KafkaAction.GET_RANDOM_CAT -> {
        var catDto = objectMapper.convertValue(request.getResult(), CatDto.class);
        telegramResponse.setText(catDto.getName() + "\nАвтор: @" + catDto.getOwner());
        telegramResponse.setPhoto(catDto.getPhoto());
        telegramResponse.setButtons(
            List.of(
                new TelegramButtonDto("👍 (" + catDto.getLikes() + ")", "like_" + catDto.getId()),
                new TelegramButtonDto(
                    "👎 (" + catDto.getDislikes() + ")", "dislike_" + catDto.getId()),
                new TelegramButtonDto("В меню", "main_menu")));
      }
      case KafkaAction.GET_CAT -> {
        var catDto = objectMapper.convertValue(request.getResult(), CatDto.class);
        telegramResponse.setText(catDto.getName() + "\nАвтор: @" + catDto.getOwner());
        telegramResponse.setPhoto(catDto.getPhoto());
        telegramResponse.setButtons(
            List.of(
                new TelegramButtonDto("Удалить", "delete_cat_" + catDto.getId()),
                new TelegramButtonDto("В меню", "main_menu")));
      }
      case KafkaAction.GET_USER_CATS -> {
        List<CatDto> catDtos =
            objectMapper.convertValue(
                request.getResult(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CatDto.class));
        telegramResponse.setText("Вот список ваших котиков");
        var buttonDtos =
            catDtos.stream()
                .map(
                    catDto -> new TelegramButtonDto(catDto.getName(), "view_cat_" + catDto.getId()))
                .collect(Collectors.toList());
        buttonDtos.add(new TelegramButtonDto("В меню", "main_menu"));
        telegramResponse.setButtons(buttonDtos);
      }
    }

    dispatcher.send(telegramResponse);
  }
}
