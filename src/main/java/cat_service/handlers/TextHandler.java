package cat_service.handlers;

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
public class TextHandler {

  private final KafkaFacade kafka;
  private final ResponseFactory responses;

  public void handle(
      TelegramRequestDto requestDto, UserSessionModel session, TelegramResponseDto response) {
    switch (session.getState()) {
      case WAITING_FOR_NAME -> {
        session.setUsername(requestDto.getText());
        session.setState(UserState.MAIN_MENU);
        responses.fillMainMenu(response, session.getUsername());

        kafka.sendGetOrCreateUser(session.getChatId(), session.getUsername());
      }
      case ADDING_CAT_NAME -> {
        session.setCatName(requestDto.getText());
        session.setState(UserState.CONFIRMING_CAT);
        responses.fillConfirmCatName(response);
      }
    }
  }
}
