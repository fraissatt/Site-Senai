package com.pdf.ist2.app.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.pdf.ist2.app.dto.UsuarioDTO;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.EmpresaRepository;
import com.pdf.ist2.app.repositories.UserRepository;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;

    public LoginController(UserRepository userRepository, EmpresaRepository empresaRepository) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
    }

    // Método para listar usuários com paginação
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_STAKEHOLDER')")
    public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(required = false) Long empresaId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Usuario> usuarios;

        // Verifica se um filtro por empresa foi aplicado
        if (empresaId != null) {
            usuarios = userRepository.findByEmpresaId(empresaId, pageable);
        } else {
            usuarios = userRepository.findAll(pageable);
        }

        Page<UsuarioDTO> usuarioDTOs = usuarios.map(usuario -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());

            if (usuario.getEmpresa() != null) {
                dto.setEmpresaId(usuario.getEmpresa().getId());
                dto.setEmpresa(usuario.getEmpresa().getNomeEmpresa());
            } else {
                dto.setEmpresaId(null);
                dto.setEmpresa("Sem empresa");
            }

            String role = usuario.getRoles().stream().findFirst().orElse("ROLE_CLIENTE");
            dto.setRole(role);
            return dto;
        });

        return ResponseEntity.ok(usuarioDTOs);
    }


    @GetMapping("/users/buscar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(@RequestParam String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Usuario> usuarios = userRepository.findByNomeContainingIgnoreCase(termo);
        List<UsuarioDTO> usuarioDTOs = usuarios.stream().map(usuario -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setEmpresa(usuario.getEmpresa() != null ? usuario.getEmpresa().getNomeEmpresa() : "Sem empresa");
            String role = usuario.getRoles().stream().findFirst().orElse("ROLE_CLIENTE");
            dto.setRole(role);
            return dto;
        }).toList();

        return ResponseEntity.ok(usuarioDTOs);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UsuarioDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já está em uso.");
        }

        Empresa empresa = empresaRepository.findById(userDTO.getEmpresaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(userDTO.getNome());
        novoUsuario.setEmail(userDTO.getEmail());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(userDTO.getSenha());
        novoUsuario.setSenha(encodedPassword);

        novoUsuario.setEmpresa(empresa);

        Set<String> roles = new HashSet<>();
        switch (userDTO.getRole()) {
            case "ROLE_ADMIN":
                roles.add("ROLE_ADMIN");
                break;
            case "ROLE_STAKEHOLDER":
                roles.add("ROLE_STAKEHOLDER");
                break;
            default:
                roles.add("ROLE_CLIENTE");
        }
        novoUsuario.setRoles(roles);

        Usuario savedUser = userRepository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UsuarioDTO updatedUserDTO) {
        try {
            Optional<Usuario> userOptional = userRepository.findById(id);
    
            if (userOptional.isPresent()) {
                Usuario user = userOptional.get();
    
                // Verificar se o e-mail já está em uso por outro usuário
                Optional<Usuario> existingUserWithEmail = userRepository.findByEmail(updatedUserDTO.getEmail());
                if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já está em uso por outro usuário.");
                }
    
                // Atualizar informações básicas
                user.setNome(updatedUserDTO.getNome());
                user.setEmail(updatedUserDTO.getEmail());
    
                // Atualizar senha somente se preenchida
                if (updatedUserDTO.getSenha() != null && !updatedUserDTO.getSenha().isBlank()) {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    user.setSenha(encoder.encode(updatedUserDTO.getSenha()));
                }
    
                // Atualizar a empresa (exceto para ROLE_ADMIN)
                if (!"ROLE_ADMIN".equals(updatedUserDTO.getRole())) {
                    if (updatedUserDTO.getEmpresaId() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empresa é obrigatória para este tipo de usuário");
                    }
    
                    Optional<Empresa> empresaOptional = empresaRepository.findById(updatedUserDTO.getEmpresaId());
                    if (empresaOptional.isPresent()) {
                        user.setEmpresa(empresaOptional.get());
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa não encontrada");
                    }
                } else {
                    user.setEmpresa(null); // ROLE_ADMIN não deve ter empresa vinculada
                }
    
                // Atualizar roles
                if (updatedUserDTO.getRole() != null) {
                    Set<String> roles = new HashSet<>();
                    roles.add(updatedUserDTO.getRole());
                    user.setRoles(roles);
                }
    
                // Salvar alterações
                userRepository.save(user);
    
                return ResponseEntity.ok("Usuário atualizado com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar o usuário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar o usuário");
        }
    }    

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        Optional<Usuario> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
    }

    @GetMapping("/users/verificar-email")
    public ResponseEntity<Boolean> verificarEmail(@RequestParam String email) {
        boolean emailExiste = userRepository.existsByEmail(email);
        return ResponseEntity.ok(emailExiste);
    }

}
