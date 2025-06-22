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
        response.setText("Привет " + sessionModel.getUsername() + "!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Добавить котика", "add_cat"),
                new TelegramButtonDto("Смотреть котиков", "view_random_cat"),
                new TelegramButtonDto("Мои котики", "my_cats")));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.GET_OR_CREATE_USER);
        kafkaMessage.setUserId(requestDto.getChatId());
        kafkaMessage.setUsername(sessionModel.getUsername());
        kafkaProducer.sendToService(kafkaMessage);
      }
      case ADDING_CAT_NAME -> {
        sessionModel.setCatName(requestDto.getText());
        sessionModel.setState(UserState.CONFIRMING_CAT);
        response.setText("Какое красивое имя! Ты хочешь его оставить?");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Подтвердить", "confirm_cat_name"),
                new TelegramButtonDto("Повторить", "add_cat"),
                new TelegramButtonDto("В меню", "main_menu")));
      }
    }
  }

  private void handleCommand(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCommand()) {
      case "/start" -> {
        sessionModel.setState(UserState.WAITING_FOR_NAME);
        response.setText("Привет, как тебя зовут?");
      }
    }
  }

  private void handlePhoto(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (sessionModel.getState()) {
      case UserState.ADDING_CAT_PHOTO -> {
        sessionModel.setCatPhoto(requestDto.getPhoto());
        sessionModel.setState(UserState.CONFIRMING_CAT);
        response.setText(sessionModel.getCatName() + "\nАвтор: @" + sessionModel.getUsername());
        response.setPhoto(sessionModel.getCatPhoto());
        response.setButtons(
            List.of(
                new TelegramButtonDto("Подтвердить", "confirm_cat"),
                new TelegramButtonDto("В меню", "main_menu")));
      }
    }
  }

  private void handleCallback(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCallback()) {
      case String callback when callback.startsWith("like_") -> {
        response.setText("Лайк сохранен");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Добавить котика", "add_cat"),
                new TelegramButtonDto("Смотреть котиков", "view_random_cat"),
                new TelegramButtonDto("Мои котики", "my_cats")));
        var catId = Long.parseLong(callback.substring("like_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.LIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case String callback when callback.startsWith("dislike_") -> {
        response.setText("Дизлайк сохранен");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Добавить котика", "add_cat"),
                new TelegramButtonDto("Смотреть котиков", "view_random_cat"),
                new TelegramButtonDto("Мои котики", "my_cats")));
        var catId = Long.parseLong(callback.substring("dislike_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.DISLIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case String callback when callback.startsWith("view_cat_") -> {
        var catId = Long.parseLong(callback.substring("view_cat_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.GET_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case String callback when callback.startsWith("delete_cat_") -> {
        var catId = Long.parseLong(callback.substring("delete_cat_".length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.DELETE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToService(kafkaMessage);
      }
      case "add_cat" -> {
        sessionModel.setState(UserState.ADDING_CAT_NAME);
        response.setText("Как зовут твоего котика?");
      }
      case "confirm_cat_name" -> {
        sessionModel.setState(UserState.ADDING_CAT_PHOTO);
        response.setText("Покажи мне фото своего котика");
      }
      case "confirm_cat" -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText("Ура, котик добавлен!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Добавить котика", "add_cat"),
                new TelegramButtonDto("Смотреть котиков", "view_random_cat"),
                new TelegramButtonDto("Мои котики", "my_cats")));
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
        var kafkaRequest = new KafkaServiceDto();
        kafkaRequest.setAction(KafkaAction.GET_RANDOM_CAT);
        kafkaRequest.setUserId(sessionModel.getChatId());
        kafkaProducer.sendToService(kafkaRequest);
      }
      case "my_cats" -> {
        var kafkaRequest = new KafkaServiceDto();
        kafkaRequest.setAction(KafkaAction.GET_USER_CATS);
        kafkaRequest.setUserId(sessionModel.getChatId());
        kafkaProducer.sendToService(kafkaRequest);
      }
      case "main_menu" -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText("Привет " + sessionModel.getUsername() + "!");
        response.setButtons(
            List.of(
                new TelegramButtonDto("Добавить котика", "add_cat"),
                new TelegramButtonDto("Смотреть котиков", "view_random_cat"),
                new TelegramButtonDto("Мои котики", "my_cats")));
      }
      default -> throw new IllegalStateException("Unexpected value: " + requestDto.getCallback());
    }
  }
}
