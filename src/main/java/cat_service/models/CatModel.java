package cat_service.models;

import lombok.Data;

@Data
public class CatModel {

  private Long id;
  private String name;
  private String photoUrl;
  private int likes;
  private int dislikes;
}
