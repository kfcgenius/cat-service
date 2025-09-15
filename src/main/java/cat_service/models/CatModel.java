package cat_service.models;

import lombok.Data;

@Data
public class CatModel {

  private Long id;
  private String name;
  private byte[] photo;
  private String owner;
  private int likes;
  private int dislikes;
}
