package cat_service.models;

import cat_service.enums.UserState;
import lombok.Data;

@Data
public class UserSessionModel {

  private Long chatId;
  private String username;
  private String catName;
  private String catPhoto;
  private UserState state;

  public void reset() {
    catName = null;
    catPhoto = null;
  }
}
