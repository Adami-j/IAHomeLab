package fr.lab.iahomelab.security.entity;


import fr.lab.iahomelab.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    protected AppUser() {
    }

}