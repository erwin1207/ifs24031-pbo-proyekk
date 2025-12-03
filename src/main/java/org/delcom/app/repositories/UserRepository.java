package org.delcom.app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findFirstByEmail(String email);
}
