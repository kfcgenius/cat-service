package cat_service.dto;

import lombok.Data;

@Data
public class CatDto {

  private Long id;
  private String name;
  private byte[] photo;
  private String owner;
  private int likes;
  private int dislikes;
}
