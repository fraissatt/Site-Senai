package com.pdf.ist2.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.UserRepository;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/index", "/css/**", "/img/**", "/PDFs/**").permitAll()
                .requestMatchers("/menuADM").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAKEHOLDER") // Admin e Stakeholder podem acessar o menu ADM
                .requestMatchers("/visualizar/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_STAKEHOLDER") // Clientes, Admins e Stakeholders podem visualizar PDFs
                
                // Restrições específicas para ROLE_ADMIN
                .requestMatchers(HttpMethod.POST, "/api/pdfs/upload/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode fazer upload de PDFs
                .requestMatchers(HttpMethod.POST, "/api/empresas").hasAuthority("ROLE_ADMIN") // Somente Admin pode criar empresas
                .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode editar empresas
                .requestMatchers(HttpMethod.DELETE, "/api/empresas/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode excluir empresas
                .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ROLE_ADMIN") // Somente Admin pode criar usuários
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode excluir usuários
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode editar usuários
                .requestMatchers(HttpMethod.PUT, "/api/agendamentos/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode atualizar agendamentos
                .requestMatchers(HttpMethod.DELETE, "/api/agendamentos/**").hasAuthority("ROLE_ADMIN") // Somente Admin pode excluir agendamentos

                // Acesso a dados de empresas para ADMIN e STAKEHOLDER
                .requestMatchers(HttpMethod.GET, "/api/empresas").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAKEHOLDER")

                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();
                    Usuario usuario = usuarioDetails.getUsuario();
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                
                    System.out.println("Role capturada no login: " + role);  // Log para verificar a role
                
                    if (role.equals("ROLE_ADMIN") || role.equals("ROLE_STAKEHOLDER")) {
                        response.sendRedirect("/menuADM");
                    } else if (role.equals("ROLE_CLIENTE")) {
                        if (usuario.getEmpresa() != null) {
                            Long empresaId = usuario.getEmpresa().getId();
                            response.sendRedirect("/visualizar/" + empresaId);
                        } else {
                            System.out.println("Erro: O cliente não tem uma empresa associada.");
                            response.sendRedirect("/login?error=true");
                        }
                    }
                })
                
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Usuario usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            
            if (usuario.getEmpresa() != null) {
                usuario.getEmpresa().getId();
            }

            return new UsuarioDetails(usuario);
        };
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/PDFs/**")
                .addResourceLocations("file:///C:/PDFs/");
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
