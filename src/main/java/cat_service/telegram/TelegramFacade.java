package cat_service.telegram;

import cat_service.configs.TelegramConfigs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramFacade implements LongPollingSingleThreadUpdateConsumer {

  private final TelegramMessageHandler handler;
  private final TelegramMessageConverter converter;
  private final TelegramConfigs configs;

  @Override
  public void consume(Update update) {
    var dto = converter.convert(update);
    handler.handle(dto);
  }

  public void Start() {
    try (var bot = new TelegramBotsLongPollingApplication()) {
      bot.registerBot(configs.getToken(), this);
      Thread.currentThread().join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
