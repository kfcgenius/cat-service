package cat_service.handlers;

import cat_service.constants.TelegramCallback;
import cat_service.constants.TelegramText;
import cat_service.dto.TelegramRequestDto;
import cat_service.dto.TelegramResponseDto;
import cat_service.enums.UserState;
import cat_service.models.UserSessionModel;
import cat_service.telegram.KafkaFacade;
import cat_service.telegram.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

  private final KafkaFacade kafka;
  private final ResponseFactory responses;

  public void handle(
      TelegramRequestDto requestDto, UserSessionModel session, TelegramResponseDto response) {
    var callback = requestDto.getCallback();

    switch (callback) {
      case String c when c.startsWith(TelegramCallback.LIKE_PREFIX) -> {
        responses.fillMenuWithText(response, TelegramText.LIKE_SAVED);
        kafka.sendLike(session.getChatId(), c.substring(TelegramCallback.LIKE_PREFIX.length()));
      }
      case String c when c.startsWith(TelegramCallback.DISLIKE_PREFIX) -> {
        responses.fillMenuWithText(response, TelegramText.DISLIKE_SAVED);
        kafka.sendDislike(
            session.getChatId(), c.substring(TelegramCallback.DISLIKE_PREFIX.length()));
      }
      case String c when c.startsWith(TelegramCallback.VIEW_CAT_PREFIX) -> {
        kafka.sendGetCat(
            session.getChatId(), c.substring(TelegramCallback.VIEW_CAT_PREFIX.length()));
      }
      case String c when c.startsWith(TelegramCallback.DELETE_CAT_PREFIX) -> {
        kafka.sendDeleteCat(
            session.getChatId(), c.substring(TelegramCallback.DELETE_CAT_PREFIX.length()));
      }
      case TelegramCallback.ADD_CAT -> {
        session.reset();
        session.setState(UserState.ADDING_CAT_NAME);
        response.setText(TelegramText.ADD_CAT);
      }
      case TelegramCallback.CONFIRM_CAT_NAME -> {
        session.setState(UserState.ADDING_CAT_PHOTO);
        response.setText(TelegramText.ASK_CAT_PHOTO);
      }
      case TelegramCallback.CONFIRM_CAT -> {
        session.setState(UserState.MAIN_MENU);
        responses.fillMenuWithText(response, TelegramText.CAT_ADDED);
        kafka.sendCreateCat(session);
      }
      case TelegramCallback.VIEW_RANDOM_CAT -> kafka.sendGetRandomCat(session.getChatId());
      case TelegramCallback.MY_CATS -> kafka.sendGetUserCats(session.getChatId());
      case TelegramCallback.MAIN_MENU -> responses.fillMainMenu(response, session.getUsername());
      default -> responses.fillError(response, callback, session.getUsername());
    }
  }
}
