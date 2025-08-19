package com.vn.gotogether.repository.user;



import com.vn.gotogether.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
    SELECT u FROM User u 
    ORDER BY 
        CASE WHEN u.role.name = 'ROLE_ADMIN' THEN 0 ELSE 1 END, 
        u.createdAt DESC
""")
    List<User> findAllUsersWithAdminFirst();

    List<User> findByRole_Name(String roleName);

}
