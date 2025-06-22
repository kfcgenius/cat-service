package cat_service.entities;

import cat_service.enums.UserState;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_sessions")
public class UserSessionEntity {

  @Id private Long chatId;
  private String username;
  private String catName;
  private String catPhoto;
  private UserState state = UserState.STARTED;

  @Column(name = "updated_at")
  private Instant updatedAt = Instant.now();

  @PreUpdate
  @PrePersist
  public void updateTimestamp() {
    updatedAt = Instant.now();
  }
}
