package cat_service.telegram;

import cat_service.configs.TelegramConfigs;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Configuration
@RequiredArgsConstructor
public class TelegramBotStarterConfiguration {

  private final TelegramConfigs configs;
  private final TelegramFacade facade;

  @Bean(destroyMethod = "close")
  public TelegramBotsLongPollingApplication telegramBot() throws Exception {
    var bot = new TelegramBotsLongPollingApplication();
    bot.registerBot(configs.getToken(), facade);
    return bot;
  }
}
