package cat_service.telegram;

import cat_service.configs.TelegramConfigs;
import cat_service.dto.TelegramRequestDto;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramMessageConverter {

  private final OkHttpTelegramClient client;
  private final TelegramConfigs configs;

  public TelegramMessageConverter(TelegramConfigs configs) {
    this.client = new OkHttpTelegramClient(configs.getToken());
    this.configs = configs;
  }

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
        try {
          File file = client.execute(new GetFile(largestPhoto.getFileId()));
          String filePath = file.getFilePath();

          String fileUrl = String.format(
              "https://api.telegram.org/file/bot%s/%s",
              configs.getToken(),  // важно, чтобы был доступен токен
              filePath
          );

          try (var in = new java.net.URL(fileUrl).openStream();
               var baos = new java.io.ByteArrayOutputStream()) {
            in.transferTo(baos);
            messageDto.setPhoto(baos.toByteArray());
          }
        } catch (IOException | TelegramApiException e) {
          throw new RuntimeException("Failed to download photo", e);
        }
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
