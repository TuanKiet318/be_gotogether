package com.vn.gotogether.utils;

import com.ibm.icu.text.Transliterator;

import java.util.Locale;
import java.util.Random;

public class SlugUtil {

    private static final Transliterator TO_ASCII = Transliterator.getInstance("NFD; [:Nonspacing Mark:] Remove; NFC");

    public static String toSlug(String input) {
        // Convert to ASCII (loại bỏ dấu tiếng Việt)
        input = input.replace('đ', 'd').replace('Đ', 'D');
        String ascii = TO_ASCII.transliterate(input);
        // Chuyển thành lowercase, thay khoảng trắng bằng dấu gạch ngang, loại bỏ ký tự đặc biệt
        String slug = ascii.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")  // loại ký tự đặc biệt
                .replaceAll("\\s+", "-")          // thay khoảng trắng bằng gạch ngang
                .replaceAll("-{2,}", "-")         // gộp nhiều gạch ngang lại
                .replaceAll("^-|-$", "");         // bỏ gạch ngang ở đầu/cuối
        return slug;
    }

    public static String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

