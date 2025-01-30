package com.pdf.ist2.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.pdf.ist2.app.entities.Usuario;

public interface UserRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Page<Usuario> findAll(Pageable pageable);

    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    Page<Usuario> findByEmpresaId(Long empresaId, Pageable pageable);

    boolean existsByEmail(String email);
}
