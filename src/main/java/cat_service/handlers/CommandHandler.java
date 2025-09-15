package cat_service.handlers;

import cat_service.constants.TelegramText;
import cat_service.dto.TelegramRequestDto;
import cat_service.dto.TelegramResponseDto;
import cat_service.enums.UserState;
import cat_service.models.UserSessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandHandler {

  public void handle(
      TelegramRequestDto requestDto, UserSessionModel session, TelegramResponseDto response) {
    switch (requestDto.getCommand()) {
      case "/start" -> {
        session.setState(UserState.WAITING_FOR_NAME);
        response.setText(TelegramText.GREETING_ASK_NAME);
      }
    }
  }
}
