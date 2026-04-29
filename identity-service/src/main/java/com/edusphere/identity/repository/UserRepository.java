package com.edusphere.identity.repository;
 
import com.edusphere.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
import java.util.List;
 
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByEmailAndIsDeletedFalse(String email);
 
    Optional<User> findByIdAndIsDeletedFalse(Long id);
 
    List<User> findAllByIsDeletedFalse();

	boolean existsByEmailAndIsDeletedFalse(String email);
}