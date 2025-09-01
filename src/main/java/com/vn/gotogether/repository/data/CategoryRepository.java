package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent ORDER BY c.name")
    List<Category> findAllWithParent();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findRootCategories();
}
