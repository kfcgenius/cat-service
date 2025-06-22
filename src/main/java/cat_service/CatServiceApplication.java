package cat_service;

import cat_service.telegram.TelegramFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatServiceApplication {

  public static void main(String[] args) {
    var context = SpringApplication.run(CatServiceApplication.class, args);
    var bot = context.getBean(TelegramFacade.class);
    bot.Start();
  }
}
