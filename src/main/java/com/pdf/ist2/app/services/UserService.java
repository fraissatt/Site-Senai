package com.pdf.ist2.app.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método genérico para criar usuários com qualquer role
    public Usuario criarUsuario(String nome, String email, String senha, String role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha)); // Criptografa a senha

        // Define a role como String
        Set<String> roles = new HashSet<>();
        roles.add(role);
        usuario.setRoles(roles);

        return userRepository.save(usuario);
    }

    // Métodos específicos para cada tipo de usuário
    public Usuario criarUsuarioAdmin(String nome, String email, String senha) {
        return criarUsuario(nome, email, senha, "ROLE_ADMIN");
    }

    public Usuario criarUsuarioCliente(String nome, String email, String senha) {
        return criarUsuario(nome, email, senha, "ROLE_CLIENTE");
    }

    public Usuario criarUsuarioStakeholder(String nome, String email, String senha) {
        return criarUsuario(nome, email, senha, "ROLE_STAKEHOLDER");
    }
}
