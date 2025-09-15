package cat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaServiceDto {

  private String action;
  private Long userId;
  private String username;
  private Long catId;
  private String name;
  private byte[] photo;
}
