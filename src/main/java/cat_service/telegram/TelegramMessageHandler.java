package cat_service.telegram;

import cat_service.constants.KafkaAction;
import cat_service.constants.TelegramCallback;
import cat_service.constants.TelegramText;
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
        response.setText(String.format(TelegramText.GREETING_USER, sessionModel.getUsername()));
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.GET_OR_CREATE_USER);
        kafkaMessage.setUserId(requestDto.getChatId());
        kafkaMessage.setUsername(sessionModel.getUsername());
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case ADDING_CAT_NAME -> {
        sessionModel.setCatName(requestDto.getText());
        sessionModel.setState(UserState.CONFIRMING_CAT);
        response.setText(TelegramText.CONFIRM_CAT_NAME);
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.BTN_CONFIRM, TelegramCallback.CONFIRM_CAT_NAME),
                new TelegramButtonDto(TelegramText.BTN_REPEAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(TelegramText.BTN_MAIN_MENU, TelegramCallback.MAIN_MENU)));
      }
    }
  }

  private void handleCommand(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCommand()) {
      case "/start" -> {
        sessionModel.setState(UserState.WAITING_FOR_NAME);
        response.setText(TelegramText.GREETING_ASK_NAME);
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
                new TelegramButtonDto(TelegramText.BTN_CONFIRM, TelegramCallback.CONFIRM_CAT),
                new TelegramButtonDto(TelegramText.BTN_MAIN_MENU, TelegramCallback.MAIN_MENU)));
      }
    }
  }

  private void handleCallback(
      TelegramRequestDto requestDto, UserSessionModel sessionModel, TelegramResponseDto response) {
    switch (requestDto.getCallback()) {
      case String callback when callback.startsWith(TelegramCallback.LIKE_PREFIX) -> {
        response.setText(TelegramText.LIKE_SAVED);
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
        var catId = Long.parseLong(callback.substring(TelegramCallback.LIKE_PREFIX.length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.LIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case String callback when callback.startsWith(TelegramCallback.DISLIKE_PREFIX) -> {
        response.setText(TelegramText.DISLIKE_SAVED);
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
        var catId = Long.parseLong(callback.substring(TelegramCallback.DISLIKE_PREFIX.length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.DISLIKE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case String callback when callback.startsWith(TelegramCallback.VIEW_CAT_PREFIX) -> {
        var catId = Long.parseLong(callback.substring(TelegramCallback.VIEW_CAT_PREFIX.length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.GET_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case String callback when callback.startsWith(TelegramCallback.DELETE_CAT_PREFIX) -> {
        var catId = Long.parseLong(callback.substring(TelegramCallback.DELETE_CAT_PREFIX.length()));
        var kafkaMessage = new KafkaServiceDto();
        kafkaMessage.setAction(KafkaAction.DELETE_CAT);
        kafkaMessage.setUserId(sessionModel.getChatId());
        kafkaMessage.setCatId(catId);
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case TelegramCallback.ADD_CAT -> {
        sessionModel.reset();
        sessionModel.setState(UserState.ADDING_CAT_NAME);
        response.setText(TelegramText.ADD_CAT);
      }
      case TelegramCallback.CONFIRM_CAT_NAME -> {
        sessionModel.setState(UserState.ADDING_CAT_PHOTO);
        response.setText(TelegramText.ASK_CAT_PHOTO);
      }
      case TelegramCallback.CONFIRM_CAT -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText(TelegramText.CAT_ADDED);
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
        var kafkaMessage =
            new KafkaServiceDto(
                KafkaAction.CREATE_CAT,
                sessionModel.getChatId(),
                sessionModel.getUsername(),
                null,
                sessionModel.getCatName(),
                sessionModel.getCatPhoto());
        kafkaProducer.sendToServiceTopic(kafkaMessage);
      }
      case TelegramCallback.VIEW_RANDOM_CAT -> {
        var kafkaRequest = new KafkaServiceDto();
        kafkaRequest.setAction(KafkaAction.GET_RANDOM_CAT);
        kafkaRequest.setUserId(sessionModel.getChatId());
        kafkaProducer.sendToServiceTopic(kafkaRequest);
      }
      case TelegramCallback.MY_CATS -> {
        var kafkaRequest = new KafkaServiceDto();
        kafkaRequest.setAction(KafkaAction.GET_USER_CATS);
        kafkaRequest.setUserId(sessionModel.getChatId());
        kafkaProducer.sendToServiceTopic(kafkaRequest);
      }
      case TelegramCallback.MAIN_MENU -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText(String.format(TelegramText.GREETING_USER, sessionModel.getUsername()));
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
      }
      default -> {
        sessionModel.setState(UserState.MAIN_MENU);
        response.setText(String.format(TelegramText.ERROR, requestDto.getCallback()));
        response.setButtons(
            List.of(
                new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
                new TelegramButtonDto(
                    TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
                new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)));
      }
    }
  }
}
