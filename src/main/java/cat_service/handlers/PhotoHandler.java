package cat_service.handlers;

import cat_service.dto.TelegramRequestDto;
import cat_service.dto.TelegramResponseDto;
import cat_service.enums.UserState;
import cat_service.models.UserSessionModel;
import cat_service.telegram.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhotoHandler {

  private final ResponseFactory responses;

  public void handle(
      TelegramRequestDto requestDto, UserSessionModel session, TelegramResponseDto response) {
    switch (session.getState()) {
      case ADDING_CAT_PHOTO -> {
        session.setCatPhoto(requestDto.getPhoto());
        session.setState(UserState.CONFIRMING_CAT);
        responses.fillConfirmCat(response, session);
      }
    }
  }
}
