package com.alibou.security.user;

import com.alibou.security.lessons.Lesson;
import com.alibou.security.token.Token;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue
    protected Integer id;
    @Column()
    @Size(min = 1, max = 64)
    protected String firstname;
    @Column()
    @Size(min = 1, max = 64)
    protected String lastname;
    @Column(unique = true)
    @Size(min = 3, max = 64)
    protected String username;
    protected String resetToken;
    protected String pictureLocation;
    @Enumerated(EnumType.STRING)
    @Column()
    protected Gender gender;
    @Enumerated(EnumType.STRING)
    protected NotificationMode notificationMode;
    @NotNull
    @Column(unique = true, nullable = false)
    @Size(min = 3, max = 254)
    protected String email;
    @NotNull
    @Column(nullable = false)
    protected String password;

    @Enumerated(EnumType.STRING)
    protected Role role;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    protected List<Token> tokens;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    protected List<Notification> notifications;

    private String notificationModev2;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
