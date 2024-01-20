package com.alibou.security.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

import static com.alibou.security.user.Permission.ADMIN_CREATE;
import static com.alibou.security.user.Permission.ADMIN_DELETE;
import static com.alibou.security.user.Permission.ADMIN_READ;
import static com.alibou.security.user.Permission.ADMIN_UPDATE;
import static com.alibou.security.user.Permission.MANAGER_CREATE;
import static com.alibou.security.user.Permission.MANAGER_DELETE;
import static com.alibou.security.user.Permission.MANAGER_READ;
import static com.alibou.security.user.Permission.MANAGER_UPDATE;
import static com.alibou.security.user.Role.ADMIN;
import static com.alibou.security.user.Role.MANAGER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebMvc
public class SecurityConfiguration implements WebMvcConfigurer {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final LogoutHandler logoutHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf()
            .disable()
//            .cors(cors -> {
//              cors.configurationSource(corsConfigurationSource());
//            })
            .authorizeHttpRequests()
            .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/lessons/getFilteredClasses",
                    "/api/v1/lessons/getHomePage",
                    "/api/v1/lessons/getCoursePage/{id}",
                    "/api/v1/users/getTeacherProfile/{id}",
                    "/api/v1/lessons/getCourseFilters",
                    "/api/v1/lessons/getLessonFilters",
                    "/api/v1/lessons/getReviews",
                    "/v2/api-docs",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/configuration/ui",
                    "/configuration/security",
                    "/swagger-ui/**",
                    "/webjars/**",
                    "/swagger-ui.html"
            )
            .permitAll()


            .requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())


            .requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
            .requestMatchers(POST, "/api/v1/management/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
            .requestMatchers(PUT, "/api/v1/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
            .requestMatchers(DELETE, "/api/v1/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())


            /* .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())

             .requestMatchers(GET, "/api/v1/admin/**").hasAuthority(ADMIN_READ.name())
             .requestMatchers(POST, "/api/v1/admin/**").hasAuthority(ADMIN_CREATE.name())
             .requestMatchers(PUT, "/api/v1/admin/**").hasAuthority(ADMIN_UPDATE.name())
             .requestMatchers(DELETE, "/api/v1/admin/**").hasAuthority(ADMIN_DELETE.name())*/


            .anyRequest()
            .authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout()
            .logoutUrl("/api/v1/auth/logout")
            .addLogoutHandler(logoutHandler)
            .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
    ;

    return http.build();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("https://frontendtest-v3.onrender.com")
            .allowedMethods("POST", "GET")
            .allowCredentials(true);
  }

//  @Bean
//  public CorsConfigurationSource corsConfigurationSource() {
//    CorsConfiguration configuration = new CorsConfiguration();
//    configuration.addAllowedOrigin("http://localhost:3002");
//    configuration.addAllowedOrigin("http://localhost:3000");
//    configuration.addAllowedOrigin("https://frontend-test-coei.vercel.app/");
//    configuration.addAllowedOrigin("https://frontend-test-coei-kaloyans-projects-3ca58d7b.vercel.app/");
//    configuration.addAllowedOrigin("https://frontend-test-coei-git-develop-kaloyans-projects-3ca58d7b.vercel.app/");
//    configuration.addAllowedMethod("*");
//    configuration.addAllowedHeader("*");
//    configuration.setAllowCredentials(true);
//    UrlBasedCorsConfigurationSource source = new
//            UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", configuration);
//    return source;
//  }

//  @Bean
//  CorsWebFilter corsFilter() {
//
//    CorsConfiguration config = new CorsConfiguration();
//
//    // Possibly...
//    // config.applyPermitDefaultValues()
//
//    config.setAllowCredentials(true);
//    config.addAllowedOrigin("https://domain1.com");
//    config.addAllowedHeader("*");
//    config.addAllowedMethod("*");
//
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", config);
//
//    return new CorsWebFilter(source);
//  }
}
