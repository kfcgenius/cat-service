package cat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaResponse {
  private String status;
  private String action;
  private Long userId;
  private Object result;
  private String error;
}
