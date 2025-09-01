package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, String> {

    @Query("SELECT d FROM Destination d ORDER BY d.name")
    List<Destination> findAllOrderByName();

    @Query("SELECT d FROM Destination d WHERE d.country = :country ORDER BY d.name")
    List<Destination> findByCountryOrderByName(String country);
}