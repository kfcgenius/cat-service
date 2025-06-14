package cat_service.telegram;

import cat_service.configs.TelegramConfigs;
import cat_service.dto.TelegramButtonDto;
import cat_service.dto.TelegramResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TelegramMessageDispatcher {

  private final TelegramClient client;

  public TelegramMessageDispatcher(TelegramConfigs configs) {
    client = new OkHttpTelegramClient(configs.getToken());
  }

  public void send(TelegramResponseDto response) {
    if (response.getPhoto() != null) {
      sendPhotoTextButtons(
          response.getChatId(), response.getPhoto(), response.getText(), response.getButtons());
      return;
    }
    if (response.getButtons() != null && response.getButtons().size() > 0) {
      sendTextButtons(response.getChatId(), response.getText(), response.getButtons());
      return;
    }

    sendText(response.getChatId(), response.getText());
  }

  public void sendPhotoTextButtons(
      Long chatId, String photoFileId, String caption, List<TelegramButtonDto> buttons) {
    var message =
        SendPhoto.builder()
            .chatId(chatId)
            .photo(new InputFile(photoFileId))
            .caption(caption)
            .build();
    message.setReplyMarkup(buildInlineKeyboard(buttons));
    try {
      client.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public void sendTextButtons(Long chatId, String text, List<TelegramButtonDto> buttons) {
    var message = SendMessage.builder().chatId(chatId).text(text).build();
    message.setReplyMarkup(buildInlineKeyboard(buttons));
    try {
      client.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void sendText(Long chatId, String text) {
    var message = SendMessage.builder().chatId(chatId).text(text).build();
    try {
      client.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private InlineKeyboardMarkup buildInlineKeyboard(List<TelegramButtonDto> buttons) {
    var keyboardRows = new ArrayList<InlineKeyboardRow>();
    var row = new InlineKeyboardRow();

    for (var button : buttons) {
      var inlineButton =
          InlineKeyboardButton.builder()
              .text(button.getText())
              .callbackData(button.getCallback())
              .build();
      row.add(inlineButton);
    }

    keyboardRows.add(row);

    var markup = InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();

    return markup;
  }
}
