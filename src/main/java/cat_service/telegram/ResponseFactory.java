package cat_service.telegram;

import cat_service.constants.TelegramCallback;
import cat_service.constants.TelegramText;
import cat_service.dto.TelegramButtonDto;
import cat_service.dto.TelegramResponseDto;
import cat_service.models.UserSessionModel;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

  public void fillMainMenu(TelegramResponseDto response, String username) {
    response.setText(String.format(TelegramText.GREETING_USER, username));
    response.setButtons(mainMenuButtons());
  }

  public void fillConfirmCatName(TelegramResponseDto response) {
    response.setText(TelegramText.CONFIRM_CAT_NAME);
    response.setButtons(List.of(
        new TelegramButtonDto(TelegramText.BTN_CONFIRM, TelegramCallback.CONFIRM_CAT_NAME),
        new TelegramButtonDto(TelegramText.BTN_REPEAT, TelegramCallback.ADD_CAT),
        new TelegramButtonDto(TelegramText.BTN_MAIN_MENU, TelegramCallback.MAIN_MENU)
    ));
  }

  public void fillConfirmCat(TelegramResponseDto response, UserSessionModel session) {
    response.setText(session.getCatName() + "\nАвтор: @" + session.getUsername());
    response.setPhoto(session.getCatPhoto());
    response.setButtons(List.of(
        new TelegramButtonDto(TelegramText.BTN_CONFIRM, TelegramCallback.CONFIRM_CAT),
        new TelegramButtonDto(TelegramText.BTN_MAIN_MENU, TelegramCallback.MAIN_MENU)
    ));
  }

  public void fillMenuWithText(TelegramResponseDto response, String text) {
    response.setText(text);
    response.setButtons(mainMenuButtons());
  }

  public void fillError(TelegramResponseDto response, String callback, String username) {
    response.setText(String.format(TelegramText.ERROR, callback));
    response.setButtons(mainMenuButtons());
  }

  private List<TelegramButtonDto> mainMenuButtons() {
    return List.of(
        new TelegramButtonDto(TelegramText.ADD_CAT, TelegramCallback.ADD_CAT),
        new TelegramButtonDto(TelegramText.VIEW_RANDOM_CAT, TelegramCallback.VIEW_RANDOM_CAT),
        new TelegramButtonDto(TelegramText.MY_CATS, TelegramCallback.MY_CATS)
    );
  }
}

