package cat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TelegramButtonDto {

  private String text;
  private String callback;
}
