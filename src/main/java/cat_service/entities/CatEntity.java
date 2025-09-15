package cat_service.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Entity
@Table(name = "cats")
@Data
public class CatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Lob private byte[] catPhoto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private UserEntity owner;

  @ManyToMany(mappedBy = "likedCats")
  private Set<UserEntity> likedByUsers = new HashSet<>();

  @ManyToMany(mappedBy = "dislikedCats")
  private Set<UserEntity> dislikedByUsers = new HashSet<>();
}
