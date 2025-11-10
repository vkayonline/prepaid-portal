package online.vkay.prepaidportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "user_login")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserLogin extends Auditable {

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Column(name = "sso_enabled")
    private boolean ssoEnabled;

    @Column(name = "two_fac_enabled")
    private boolean twoFaEnabled;

    @Column(name = "two_fac_type")
    private String twoFaType;

    private String totpSecret;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserLogin userLogin = (UserLogin) o;
        return getId() != null && Objects.equals(getId(), userLogin.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
