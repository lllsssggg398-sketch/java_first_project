package com.atguigu.lease.web.app.custom.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("APP接口")
                        .version("1.0")
                        .description("用户端APP接口")
                        .termsOfService("http://doc.xiaominfo.com")
                        .license(new License().name("Apache 2.0")
                                .url("http://doc.xiaominfo.com")))
                .components(new Components().addSecuritySchemes("access_token", new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)));
    }

    @Bean
    public GroupedOpenApi userAPI() {
        return GroupedOpenApi.builder().group("用户信息").
                addOperationCustomizer((operation, handlerMethod) -> operation.addSecurityItem(new SecurityRequirement().addList("access_token"))).
                pathsToMatch("/app/user/**").
                build();
    }

    @Bean
    public GroupedOpenApi loginAPI() {
        return GroupedOpenApi.builder().group("登录信息").
                addOperationCustomizer((operation, handlerMethod) -> operation.addSecurityItem(new SecurityRequirement().addList("access_token"))).
                pathsToMatch("/app/login/**", "/app/info").
                build();
    }

    @Bean
    public GroupedOpenApi personAPI() {
        return GroupedOpenApi.builder().group("个人信息").
                addOperationCustomizer((operation, handlerMethod) -> operation.addSecurityItem(new SecurityRequirement().addList("access_token"))).
                pathsToMatch(
                        "/app/history/**",
                        "/app/appointment/**",
                        "/app/agreement/**"
                ).
                build();
    }

    @Bean
    public GroupedOpenApi lookForRoomAPI() {
        return GroupedOpenApi.builder().group("找房信息").
                addOperationCustomizer((operation, handlerMethod) -> operation.addSecurityItem(new SecurityRequirement().addList("access_token"))).
                pathsToMatch(
                        "/app/apartment/**",
                        "/app/room/**",
                        "/app/payment/**",
                        "/app/region/**",
                        "/app/term/**"
                ).
                build();
    }
}
