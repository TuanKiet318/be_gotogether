package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT c, COUNT(p) as placeCount FROM Category c " +
            "LEFT JOIN c.places p " +
            "WHERE p.destination.id = :destinationId " +
            "GROUP BY c.id, c.name " +
            "ORDER BY c.name")
    List<Object[]> findCategoriesWithPlaceCountByDestination(@Param("destinationId") String destinationId);

    @Query("SELECT c FROM Category c ORDER BY c.name")
    List<Category> findAllOrderByName();
}
