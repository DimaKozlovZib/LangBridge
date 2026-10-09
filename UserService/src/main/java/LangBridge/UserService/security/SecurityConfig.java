package LangBridge.UserService.security;

import LangBridge.UserService.config.AppProperties;
import LangBridge.UserService.config.KeyProvider;
import LangBridge.UserService.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import LangBridge.UserService.config.SecurityProblemSupport;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(AppProperties props) {
        return new BCryptPasswordEncoder(props.security().bcryptStrength());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProblemSupport problemSupport) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                        .requestMatchers("/actuator/health/**", "/.well-known/jwks.json").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(problemSupport)
                        .accessDeniedHandler(problemSupport)
                );
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyProvider keyProvider) {
        return keyProvider.jwtEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyProvider keyProvider) {
        return keyProvider.jwtDecoder();
    }
}
