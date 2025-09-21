// src/main/java/com/vn/gotogether/repository/data/FavoritePlaceRepository.java
package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.FavoritePlace;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, String> {

    boolean existsByUser_IdAndPlace_Id(String userId, String placeId);

    void deleteByUser_IdAndPlace_Id(String userId, String placeId);

    long countByPlace_Id(String placeId);

    @Query("""
        select fp.place.id
        from FavoritePlace fp
        where fp.user.id = :userId and fp.place.id in :placeIds
    """)
    List<String> findFavoritedPlaceIds(@Param("userId") String userId, @Param("placeIds") List<String> placeIds);

    @Query("""
        select fp.place.id, count(fp.id)
        from FavoritePlace fp
        where fp.place.id in :placeIds
        group by fp.place.id
    """)
    List<Object[]> countByPlaceIds(@Param("placeIds") List<String> placeIds);

    @Query("""
        select fp.place.id
        from FavoritePlace fp
        where fp.user.id = :userId
    """)
    List<String> findPlaceIdsByUserId(@Param("userId") String userId);

    // Lấy bản ghi cùng Place (để build summary nhanh)
    @Query("""
        select fp
        from FavoritePlace fp
        join fetch fp.place p
        where fp.user.id = :userId
        order by fp.createdAt desc
    """)
    List<FavoritePlace> findByUserIdFetchPlace(@Param("userId") String userId);
}
