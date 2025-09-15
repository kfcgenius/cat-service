package cat_service.telegram;

import cat_service.dto.*;
import cat_service.handlers.CallbackHandler;
import cat_service.handlers.CommandHandler;
import cat_service.handlers.PhotoHandler;
import cat_service.handlers.TextHandler;
import cat_service.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramMessageHandler {

  private final CommandHandler commandHandler;
  private final TextHandler textHandler;
  private final PhotoHandler photoHandler;
  private final CallbackHandler callbackHandler;
  private final TelegramMessageDispatcher dispatcher;
  private final UserSessionService sessionService;

  public void handle(TelegramRequestDto requestDto) {
    var response = new TelegramResponseDto();
    response.setChatId(requestDto.getChatId());

    var sessionModel = sessionService.get(requestDto.getChatId());

    if (requestDto.getCallback() != null) {
      callbackHandler.handle(requestDto, sessionModel, response);
    } else if (requestDto.getPhoto() != null) {
      photoHandler.handle(requestDto, sessionModel, response);
    } else if (requestDto.getCommand() != null) {
      commandHandler.handle(requestDto, sessionModel, response);
    } else if (requestDto.getText() != null) {
      textHandler.handle(requestDto, sessionModel, response);
    }

    dispatcher.send(response);
    sessionService.save(sessionModel);
  }
}
