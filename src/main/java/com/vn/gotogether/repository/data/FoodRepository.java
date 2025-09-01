package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, String> {

    @Query("SELECT f FROM Food f JOIN FETCH f.destination WHERE f.destination.id = :destinationId ORDER BY f.name")
    List<Food> findByDestinationIdWithDestination(@Param("destinationId") String destinationId);

    @Query("SELECT f FROM Food f JOIN FETCH f.destination ORDER BY f.name")
    List<Food> findAllWithDestination();
}