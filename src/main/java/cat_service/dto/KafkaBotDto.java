package cat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaBotDto {

  private String status;
  private String action;
  private Long userId;
  private String error;
  private Object result;
}
