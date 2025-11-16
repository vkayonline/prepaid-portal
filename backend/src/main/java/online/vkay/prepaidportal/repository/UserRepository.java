package online.vkay.prepaidportal.repository;

import online.vkay.prepaidportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}