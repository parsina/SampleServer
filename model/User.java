package com.coin.app.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.coin.app.model.auth.Authority;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Table(name = "\"User\"")

public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private @NonNull String username;

    @JsonIgnore
    private @NonNull String password;

    private @NonNull String email;

    private @NotNull Date createdDate;

    private @NonNull Long parentId;

    private boolean enabled = true;

    private @NotNull String confirmationToken;

    @Enumerated(EnumType.STRING)
    private @NotNull UserRole role;

    @Enumerated(EnumType.STRING)
    private @NotNull UserStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Account account;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserInfo userInfo;


    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id"))
    private List<Authority> authorities;

    public User()
    {
    }

    public User(String email, String password)
    {
        this.email = email;
        this.password = password;
        this.createdDate = new Date();
        this.role = UserRole.ROLE_USER;
        this.status = UserStatus.INACTIVE;
    }
}
