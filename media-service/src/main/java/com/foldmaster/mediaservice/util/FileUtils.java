package com.foldmaster.mediaservice.util;

import org.springframework.web.multipart.MultipartFile;

public final class FileUtils {

    private FileUtils() {}

    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}