package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.FoodPlace;
import com.vn.gotogether.entity.Place;
import com.vn.gotogether.model.FoodPlaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodPlaceRepository extends JpaRepository<FoodPlace, FoodPlaceId> {

    @Query("SELECT fp.place FROM FoodPlace fp " +
            "WHERE fp.foodId = :foodId AND fp.place.category.id = 'cat-restaurant'")
    List<Place> findRestaurantsByFoodId(@Param("foodId") String foodId);
}

