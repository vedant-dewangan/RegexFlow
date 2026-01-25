package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
}
