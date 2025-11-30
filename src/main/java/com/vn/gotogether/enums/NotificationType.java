package com.vn.gotogether.enums;

public enum NotificationType {
    BLOG_LIKE("Lượt thích bài viết"),
    BLOG_COMMENT("Bình luận mới"),
    TOUR_REGISTRATION("Đăng ký tour"),
    TOUR_STATUS_CHANGE("Trạng thái tour thay đổi"),
    ITINERARY_INVITE("Lời mời tham gia lịch trình"),
    ITINERARY_SHARED("Chia sẻ lịch trình"),
    PLACE_REVIEW("Đánh giá địa điểm"),
    GUIDE_APPLICATION_STATUS("Trạng thái đơn đăng ký hướng dẫn viên"),
    SYSTEM("Thông báo hệ thống");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

