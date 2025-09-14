package cat_service.dto;

import lombok.Data;

@Data
public class CatDto {

  private Long id;
  private String name;
  private String photoUrl;
  private String owner;
  private int likes;
  private int dislikes;
}
