package cat_service.telegram;

import cat_service.dto.TelegramRequestDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramMessageConverter {

  public TelegramRequestDto convert(Update update) {
    if (update == null) {
      throw new IllegalArgumentException("Update must not be null");
    }

    var messageDto = new TelegramRequestDto();

    if (update.hasMessage()) {
      var message = update.getMessage();
      messageDto.setChatId(message.getFrom().getId());

      if (message.hasPhoto()) {
        var photos = message.getPhoto();
        var largestPhoto = photos.getLast();
        messageDto.setPhoto(largestPhoto.getFileId());
      }

      if (message.hasText()) {
        if (message.getText().startsWith("/")) {
          messageDto.setCommand(message.getText().split(" ")[0]);
        } else {
          messageDto.setText(message.getText());
        }
      }
    }

    if (update.hasCallbackQuery()) {
      var callback = update.getCallbackQuery();
      messageDto.setChatId(callback.getFrom().getId());
      messageDto.setCallback(callback.getData());
    }

    return messageDto;
  }
}
