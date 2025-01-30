package com.pdf.ist2.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.pdf.ist2.app.entities.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByNomeEmpresa(String nomeEmpresa);
    Optional<Empresa> findByEmail(String email);
    List<Empresa> findByVisivelStakeholder(boolean visivelStakeholder);
    
    @Override
    @NonNull
    Page<Empresa> findAll(@NonNull Pageable pageable);

    @NonNull
    Page<Empresa> findByEmpresaEstadoAndVisivelStakeholder(String estado, boolean visivelStakeholder, Pageable pageable);
    Page<Empresa> findByVisivelStakeholder(boolean visivelStakeholder, @NonNull Pageable pageable);

    Page<Empresa> findByNomeEmpresaContainingIgnoreCase(String termo, Pageable pageable);
    
    List<Empresa> findByNomeEmpresaContainingIgnoreCase(String termo);

    @Query("SELECT e FROM Empresa e")
    List<Empresa> listarTodasEmpresas();

    List<Empresa> findByEmpresaEstado(String estado);

}
