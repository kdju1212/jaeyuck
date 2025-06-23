package com.office.cook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 정적 리소스 매핑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("classpath:/static/resources/");

        registry
            .addResourceHandler("/libraryUploadImg/**")
            .addResourceLocations("file:///C:/library/upload/");
    }
}
