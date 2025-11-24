package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.CategoryDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryDefaultRepository extends JpaRepository<CategoryDefault, String> {
    Optional<CategoryDefault> findByCategoryId(String categoryId);
}
