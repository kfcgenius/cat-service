package cat_service.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"cats", "likedCats", "dislikedCats"})
@ToString(exclude = {"cats", "likedCats", "dislikedCats"})
public class UserEntity {

  @Id private Long id;

  @Column(nullable = false)
  private String username;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CatEntity> cats = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "user_liked_cats",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "cat_id"))
  private Set<CatEntity> likedCats = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "user_disliked_cats",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "cat_id"))
  private Set<CatEntity> dislikedCats = new HashSet<>();
}
