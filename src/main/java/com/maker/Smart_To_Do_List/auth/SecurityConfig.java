package com.maker.Smart_To_Do_List.auth;

import com.maker.Smart_To_Do_List.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    @Value("${jwt.secret}")
    private String secretKey;

    // 그냥 Spring.Security 사용하면 안되기 때문에 상속 받아서 재정의
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                    .httpBasic().disable() // UI쪽으로 들어오는거 disable
                    .csrf().disable() // csrf공격 disable
                    .cors()
                .and() // cors 허용
                    .authorizeRequests()
                    .antMatchers("/api/v1/user/join","/api/v1/user/join/id","/api/v1/user/join/username","/api/v1/user/login", "/api/v1/user/refresh").permitAll() // 토큰 없이 허용
                    .antMatchers(HttpMethod.POST,"/api/v1/**").authenticated() // 토큰 받아야 허용

                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt 사용할 때 씀
                .and()
                    .addFilterBefore(new JwtFilter(userService,secretKey), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
