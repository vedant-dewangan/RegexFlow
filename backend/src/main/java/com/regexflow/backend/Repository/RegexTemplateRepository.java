package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.RegexTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegexTemplateRepository extends JpaRepository<RegexTemplate,Long> {
}
