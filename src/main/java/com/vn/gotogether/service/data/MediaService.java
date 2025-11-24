package com.vn.gotogether.service.data;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    /**
     * Upload file lên Cloudinary
     * @param file MultipartFile
     * @param userId ID của user (dùng để tạo folder)
     * @param folder Folder con (itinerary hoặc blog)
     * @return URL của file đã upload
     */
    public String uploadMedia(MultipartFile file, String userId, String folder) {
        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File không được để trống");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
                throw new IllegalArgumentException("Chỉ chấp nhận file ảnh hoặc video");
            }

            // Xác định resource_type
            String resourceType = contentType.startsWith("image/") ? "image" : "video";

            // Upload lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "gotogether/" + folder + "/" + userId,
                            "resource_type", resourceType
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            log.info("Upload successful: {}", url);

            return url;

        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage());
            throw new RuntimeException("Upload thất bại: " + e.getMessage());
        }
    }

    /**
     * Tạo thumbnail cho video
     */
    public String generateThumbnail(String videoUrl) {
        try {
            // Extract public_id từ URL
            String publicId = extractPublicId(videoUrl);

            // Tạo thumbnail URL
            return cloudinary.url()
                    .resourceType("video")
                    .format("jpg")
                    .transformation(
                            new Transformation()
                                    .startOffset("0")
                                    .duration("1")
                                    .width(400)
                                    .height(300)
                                    .crop("fill")
                    )
                    .generate(publicId);


        } catch (Exception e) {
            log.error("Generate thumbnail failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Xóa file từ Cloudinary
     */
    public boolean deleteMedia(String url) {
        try {
            String publicId = extractPublicId(url);
            Map result = cloudinary.uploader().destroy(publicId, Map.of());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            return false;
        }
    }

    private String extractPublicId(String url) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/.../gotogether/blogs/user123/abc123.jpg
        // Return: gotogether/blogs/user123/abc123

        String[] parts = url.split("/");
        int uploadIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i]) || "video".equals(parts[i])) {
                uploadIndex = i;
                break;
            }
        }

        if (uploadIndex == -1 || uploadIndex + 1 >= parts.length) {
            throw new IllegalArgumentException("Invalid Cloudinary URL");
        }

        StringBuilder publicId = new StringBuilder();
        for (int i = uploadIndex + 2; i < parts.length; i++) {
            if (i > uploadIndex + 2) publicId.append("/");

            // Remove extension from last part
            if (i == parts.length - 1) {
                String lastPart = parts[i];
                int dotIndex = lastPart.lastIndexOf('.');
                publicId.append(dotIndex > 0 ? lastPart.substring(0, dotIndex) : lastPart);
            } else {
                publicId.append(parts[i]);
            }
        }

        return publicId.toString();
    }
}