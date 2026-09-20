package com.learn.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_permissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_permission", columnNames = {"user_id", "permission_id"})
)
@Check(constraints = "effect IN ('ALLOW', 'DENY')")
public class UserPermission extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false, length = 10)
    private PermissionEffect effect;

    public UserPermission(User user, Permission permission, PermissionEffect effect) {
        this.user = user;
        this.permission = permission;
        this.effect = effect;
    }
}
