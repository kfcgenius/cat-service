package cat_service.dto;

import lombok.Data;

@Data
public class CatDto {

  private Long id;
  private String name;
  private String photoUrl;
  private int likes;
  private int dislikes;
}
