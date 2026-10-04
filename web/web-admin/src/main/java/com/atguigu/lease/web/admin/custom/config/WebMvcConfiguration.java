package com.atguigu.lease.web.admin.custom.config;

import com.atguigu.lease.web.admin.custom.converter.StringToBaseEnumConverterFactory;
import com.atguigu.lease.web.admin.custom.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

    @Configuration
    public class WebMvcConfiguration implements WebMvcConfigurer {

        @Autowired
        private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
        }
        @Autowired
        private AuthenticationInterceptor authenticationInterceptor;

        @Value("${admin.auth.path-patterns.include}")
        private String[] includePathPatterns;

        @Value("${admin.auth.path-patterns.exclude}")
        private String[] excludePathPatterns;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(this.authenticationInterceptor).addPathPatterns(includePathPatterns).excludePathPatterns(excludePathPatterns);
        }
    }

