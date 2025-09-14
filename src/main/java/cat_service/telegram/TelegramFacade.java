package cat_service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramFacade implements LongPollingSingleThreadUpdateConsumer {

  private final TelegramMessageHandler handler;
  private final TelegramMessageConverter converter;

  @Override
  public void consume(Update update) {
    var dto = converter.convert(update);
    handler.handle(dto);
  }
}
