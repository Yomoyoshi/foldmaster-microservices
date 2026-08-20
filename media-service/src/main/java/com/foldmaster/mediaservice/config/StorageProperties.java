package com.foldmaster.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {
    /**
     * Папка для хранения загруженных файлов.
     */
    private String location = "./uploads";
}