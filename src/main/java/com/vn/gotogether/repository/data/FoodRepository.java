package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, String> {

    List<Food> findByDestinationIdOrderByName(String destinationId);
}

