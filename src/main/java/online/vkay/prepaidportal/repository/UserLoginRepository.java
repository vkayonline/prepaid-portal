package online.vkay.prepaidportal.repository;

import aj.org.objectweb.asm.commons.Remapper;
import online.vkay.prepaidportal.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
    Optional<UserLogin> findByUsername(String username);
}