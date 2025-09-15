package cat_service;

import cat_service.telegram.TelegramBotStarterConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(TelegramBotStarterConfiguration.class)
public class CatServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CatServiceApplication.class, args);
  }
}
