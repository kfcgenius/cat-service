package cat_service.telegram;

import cat_service.constants.KafkaAction;
import cat_service.dto.*;
import cat_service.enums.UserState;
import cat_service.kafka.KafkaMessageProducer;
import cat_service.models.UserSessionModel;
import cat_service.services.UserSessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramMessageHandler {

  private final TelegramMessageDispatcher dispatcher;
  private final UserSessionService sessionService;
  private final KafkaMessageProducer kafkaProducer;

  public void handle(TelegramRequestDto requestDto) {
    var response = new TelegramResponseDto();
    response.setChatId(requestDto.getChatId());

    var sessionModel = sessionService.get(requestDto.getChatId());

    if (requestDto.getCallback() != null) {
      handleCallback(requestDto, sessionModel, response);
    } else if (requestDto.getPhoto() != null) {
      handlePhoto(requestDto, sessionModel, response);
    } else if (requestDto.getCommand() != null) {
      handleCommand(requestDto, sessionModel, response);
    } else if (requestDto.getText() != null) {
      handleText(requestDto, sessionModel, response);
    }

    dispatcher.send(response);
    sessionService.save(sessionModel);
  }

  private void handleText(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (sessionModel.getState()) {
      case UserState.WAITING_FOR_NAME -> {
        sessionModel.setUsername(requestDto.getText());
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText("Hello, " + sessionModel.getUsername() + "!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Add cat", "add_cat"),
                new TelegramButtonDto("Random cat", "view_random_cat")));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.GET_OR_CREATE_USER);
        kafkaMessage.setUserId(requestDto.getChatId());
        kafkaMessage.setUsername(sessionModel.getUsername());
        kafkaProducer.sendToService(kafkaMessage);
      }
      case ADDING_CAT_NAME -> {
        sessionModel.setCatName(requestDto.getText());
        sessionModel.setState(UserState.CONFIRMING_CAT);
        response.setText(
            "Cat name: " + sessionModel.getCatName() + "\nPlease confirm or enter again.");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Confirm", "confirm_cat_name"),
                new TelegramButtonDto("Again", "add_cat"),
                new TelegramButtonDto("\uD83C\uDFE0 Menu", "main_menu")));
      }
    }
  }

  private void handleCommand(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCommand()) {
      case "/start" -> {
        sessionModel.setState(UserState.WAITING_FOR_NAME);
        response.setText("Welcome! What's your name?");
      }
    }
  }

  private void handlePhoto(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (sessionModel.getState()) {
      case UserState.ADDING_CAT_PHOTO -> {
        sessionModel.setCatPhoto(requestDto.getPhoto());
        sessionModel.setState(UserState.CONFIRMING_CAT);
        response.setText("Here is your cat: " + sessionModel.getCatName());
        response.setPhoto(sessionModel.getCatPhoto());
        response.setButtons(
            List.of(
                new TelegramButtonDto("Confirm", "confirm_cat"),
                new TelegramButtonDto("\uD83C\uDFE0 Menu", "main_menu")));
      }
    }
  }

  private void handleCallback(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCallback()) {
      case String callback when callback.startsWith("like_") -> {
        response.setText("You liked the cat!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Add cat", "add_cat"),
                new TelegramButtonDto("View random cat", "view_random_cat")));
        var catId = Long.parseLong(callback.substring("like_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.LIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case String callback when callback.startsWith("dislike_") -> {
        response.setText("You disliked the cat.");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Add cat", "add_cat"),
                new TelegramButtonDto("View random cat", "view_random_cat")));
        var catId = Long.parseLong(callback.substring("dislike_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.DISLIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case "add_cat" -> {
        sessionModel.setState(UserState.ADDING_CAT_NAME);
        response.setText("What's the cat's name?");
      }
      case "confirm_cat_name" -> {
        sessionModel.setState(UserState.ADDING_CAT_PHOTO);
        response.setText("Please send a photo of " + sessionModel.getCatName());
      }
      case "confirm_cat" -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText("Cat saved successfully!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Add cat", "add_cat"),
                new TelegramButtonDto("\uD83C\uDFE0 Menu", "main_menu")));
        var kafkaMessage =
            new KafkaServiceDto(
                KafkaAction.CREATE_CAT,
                sessionModel.getChatId(),
                sessionModel.getUsername(),
                null,
                sessionModel.getCatName(),
                sessionModel.getCatPhoto());
        kafkaProducer.sendToService(kafkaMessage);
      }
      case "view_random_cat" -> {
        response.setText("Fetching a random cat for you...");
        var kafkaRequest = new KafkaServiceDto();
        kafkaRequest.setAction(KafkaAction.GET_RANDOM_CAT);
        kafkaRequest.setUserId(sessionModel.getChatId());
        kafkaProducer.sendToService(kafkaRequest);
      }
      case "main_menu" -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText("Hello, " + sessionModel.getUsername() + "!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Add cat", "add_cat"),
                new TelegramButtonDto("Random cat", "view_random_cat")));
      }
      default -> throw new IllegalStateException("Unexpected value: " + requestDto.getCallback());
    }
  }
}
