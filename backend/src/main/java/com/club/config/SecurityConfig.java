package com.club.config;

import com.club.common.RoleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // 启用 @PreAuthorize 细粒度权限控制
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口：登录、注册、密码重置
                .requestMatchers(
                    "/api/auth/**",
                    "/api/clubs/dashboard",
                    "/api/admin/export/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // 用户管理列表和基础配置仅管理员可访问
                .requestMatchers("/api/user/list", "/api/admin/config/**").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers("/api/user/*/role").hasRole(RoleConstants.ADMIN)
                // 社团负责人需要有数据看板和社团管理的权限
                .requestMatchers("/api/admin/stat/**", "/api/admin/dashboard/stats", "/api/admin/clubs/**").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers("/api/admin/**").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers("/api/user/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER, RoleConstants.MEMBER)
                // 活动管理权限
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/activities").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers("/api/activities/*/finish", "/api/activities/*/reply").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 活动审核仅社联/管理员
                .requestMatchers("/api/activities/*/audit").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                // 志愿服务接口权限
                .requestMatchers("/api/volunteer/export/**").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers("/api/volunteer/stats/ranking", "/api/volunteer/stats/user/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers("/api/volunteer/stats/summary", "/api/volunteer/records", "/api/volunteer/pending", "/api/volunteer/*/audit").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 投票创建和关闭仅社团负责人
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/votes").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers("/api/votes/*/close").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 场地管理仅管理员
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/venues").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/venues").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/venues/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                // 场地预约仅社团负责人
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/venues/bookings").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 场地审批仅管理员
                .requestMatchers("/api/venues/bookings/*/audit").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers("/api/venues/bookings/pending").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                .requestMatchers("/api/venues/bookings/all").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN)
                // 设备巡检模块：设备和计划管理仅社团负责人及以上
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/inspections/devices").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/inspections/devices").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/inspections/devices/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/inspections/plans").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/inspections/plans").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/inspections/plans/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 社团据点管理：写操作仅管理员/社联/社团负责人，读操作登录后可访问
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/club-locations").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/club-locations").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/club-locations/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 导师制模块：导师和时段的写操作仅社团负责人及以上
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/mentorship/mentors").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/mentorship/mentors").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/mentorship/mentors/*").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/mentorship/slots").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                .requestMatchers("/api/mentorship/slots/*/cancel").hasAnyRole(
                    RoleConstants.ADMIN, RoleConstants.UNION_ADMIN, RoleConstants.CLUB_LEADER)
                // 其余接口需登录
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
