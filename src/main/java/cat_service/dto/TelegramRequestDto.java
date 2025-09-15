package cat_service.dto;

import lombok.Data;

@Data
public class TelegramRequestDto {

  private Long chatId;
  private String text;
  private String command;
  private String callback;
  private byte[] photo;
}
