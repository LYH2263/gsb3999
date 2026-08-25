package com.agritrace.repository;
import com.agritrace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    long countByRoleAndEnabled(String role, Integer enabled);
}
