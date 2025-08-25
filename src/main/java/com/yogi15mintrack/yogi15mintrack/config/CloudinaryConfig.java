package com.yogi15mintrack.yogi15mintrack.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dwc2jpfbw");
        config.put("api_key", "267941759496368");
        config.put("api_secret", "AHCfy5NVjFz8wU4d46z26sTga-4");
        return new Cloudinary(config);
    }
}

