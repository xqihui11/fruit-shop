package com.fruitshop.config;

import com.fruitshop.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/login", 
                    "/user/register", 
                    "/product/list", 
                    "/product/detail/**", 
                    "/category/list",
                    "/review/product/**",
                    "/review/merchant/**",
                    "/merchant/login",
                    "/merchant/register",
                    "/admin/login",
                    "/payment/callback"
                );
    }
}

