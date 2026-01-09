package com.example.spring_radis_token_validation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return  http
                .formLogin ( AbstractHttpConfigurer::disable )
                .httpBasic ( AbstractHttpConfigurer::disable )
                .csrf ( AbstractHttpConfigurer::disable )
                .authorizeHttpRequests (a->
                        a.requestMatchers ( "/**" ).permitAll ()
                )
                .headers(headers -> headers
                        .frameOptions( HeadersConfigurer.FrameOptionsConfig::sameOrigin )
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("X-Custom-Header", "value");
                            response.setHeader("X-App-Version", "1.0.0");
                            response.setHeader("X-Env", "local");
                        })
                )
                .sessionManagement (session->
                        session.sessionCreationPolicy ( SessionCreationPolicy.STATELESS )
                )
                .build ();
    }
    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins( List.of ( "*" ) );

        configuration.setAllowedMethods( Arrays.asList("GET","POST","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.setAllowedOriginPatterns( List.of("http://localhost:3000", "http://your-frontend-domain.com"));
//        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setExposedHeaders(List.of("Authorization"));
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source); // Corrected this line as discussed previously
//    }
}
