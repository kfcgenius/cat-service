package cat_service.dto;

import java.util.List;
import lombok.Data;

@Data
public class TelegramResponseDto {

  private Long chatId;
  private String text;
  private String photo;
  private List<TelegramButtonDto> buttons;
}
