package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, String> {

    // Đếm tổng item của một itinerary (đang OK)
    long countByItinerary_Id(String itineraryId);

    // Liệt kê toàn bộ item theo lộ trình (đang OK)
    List<ItineraryItem> findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(String itineraryId);

    // Liệt kê item theo NGÀY (phục vụ view/drag-drop)
    List<ItineraryItem> findByItinerary_IdAndDayNumberOrderByOrderInDayAsc(String itineraryId, Integer dayNumber);

    // Tìm highest orderInDay trong NGÀY (đang có, thêm @Param cho chắc)
    @Query("""
        select max(ii.orderInDay)
        from ItineraryItem ii
        where ii.itinerary.id = :itineraryId and ii.dayNumber = :dayNumber
    """)
    Integer findTopOrderInDayByItineraryAndDay(@Param("itineraryId") String itineraryId,
                                               @Param("dayNumber") Integer dayNumber);

    // Tiện ích: lấy max order hoặc 0 nếu null (dùng khi APPEND)
    default int findMaxOrderInDayOrZero(String itineraryId, Integer dayNumber) {
        Integer max = findTopOrderInDayByItineraryAndDay(itineraryId, dayNumber);
        return (max == null) ? 0 : max;
    }

    // Chống trùng khi import: trả về danh sách placeId đã có trong NGÀY
    @Query("""
        select i.place.id
        from ItineraryItem i
        where i.itinerary.id = :itineraryId and i.dayNumber = :dayNumber
    """)
    List<String> findPlaceIdsInDay(@Param("itineraryId") String itineraryId,
                                   @Param("dayNumber") Integer dayNumber);

    // Reindex ổn định: sắp theo startTime (null lên trước) rồi orderInDay
    // (JPQL không portable "nulls first", dùng CASE để tương thích)
    @Query("""
        select i
        from ItineraryItem i
        where i.itinerary.id = :itineraryId and i.dayNumber = :dayNumber
        order by case when i.startTime is null then 0 else 1 end,
                 i.startTime asc,
                 i.orderInDay asc
    """)
    List<ItineraryItem> findAllByItineraryAndDayForReindex(@Param("itineraryId") String itineraryId,
                                                           @Param("dayNumber") Integer dayNumber);

    // Ràng buộc vị trí: tiện check uniqueness nếu bạn thêm unique ở DB
    boolean existsByItinerary_IdAndDayNumberAndOrderInDay(String itineraryId, Integer dayNumber, Integer orderInDay);

    void deleteAllByItinerary_Id(String itineraryId);

}
