package com.pdf.ist2.app.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pdf.ist2.app.dto.RelatorioStatusDTO;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.PDF;

@Repository
public interface PDFRepository extends JpaRepository<PDF, Long> {
    // Método para encontrar PDFs por empresa
    List<PDF> findByEmpresaId(Long empresaId);

    // Método para verificar se há duplicatas de arquivos no database
    List<PDF> findByEmpresaIdAndFileName(Long empresaId, String fileName);

    // Método para encontrar PDF por nome do arquivo e empresa (para evitar duplicidade)
    Optional<PDF> findByFileNameAndEmpresa(String fileName, Empresa empresa);

    @Query("SELECT new com.pdf.ist2.app.dto.RelatorioStatusDTO(p.estado, COUNT(p)) " +
           "FROM PDF p GROUP BY p.estado")
    List<RelatorioStatusDTO> countRelatoriosByStatus();

    Long countByEmpresaId(Long empresaId);

    List<PDF> findByEmpresaIdAndFileNameContainingIgnoreCase(Long empresaId, String fileName);


    Page<PDF> findByEmpresaId(Long empresaId, Pageable pageable);

    Page<PDF> findByEmpresaIdAndFileNameContainingIgnoreCase(Long empresaId, String fileName, Pageable pageable);

    @Query(value = "SELECT * FROM pdf p WHERE p.empresa_id = :empresaId " +
                "AND (:status IS NULL OR " +
                "     (:status = 'ativo' AND p.data_validade > CURRENT_DATE) OR " +
                "     (:status = 'prestes a vencer' AND p.data_validade BETWEEN CURRENT_DATE AND :dataLimite) OR " +
                "     (:status = 'expirado' AND p.data_validade < CURRENT_DATE)) " +
                "AND (:estado IS NULL OR p.estado = :estado) " +
                "AND (:natureza IS NULL OR p.natureza = :natureza) " +
                "AND (:fileName IS NULL OR p.file_name ILIKE CONCAT('%', :fileName, '%'))",
        nativeQuery = true)
    List<PDF> findByEmpresaIdAndFiltrosComNomeSemPaginacao(
            @Param("empresaId") Long empresaId,
            @Param("status") String status,
            @Param("estado") String estado,
            @Param("natureza") String natureza,
            @Param("fileName") String fileName,
            @Param("dataLimite") LocalDate dataLimite);
            
    @Query("SELECT p FROM PDF p WHERE p.empresa.id = :empresaId " +
        "AND (:status IS NULL OR " +
        "     (:status = 'ativo' AND p.dataValidade > CURRENT_DATE) OR " +
        "     (:status = 'prestes a vencer' AND p.dataValidade BETWEEN CURRENT_DATE AND :dataLimite) OR " +
        "     (:status = 'expirado' AND p.dataValidade < CURRENT_DATE)) " +
        "AND (:estado IS NULL OR p.estado = :estado) " +
        "AND (:natureza IS NULL OR p.natureza = :natureza)")
    Page<PDF> findByEmpresaIdAndFiltros(@Param("empresaId") Long empresaId,
                                        @Param("status") String status,
                                        @Param("estado") String estado,
                                        @Param("natureza") String natureza,
                                        @Param("dataLimite") LocalDate dataLimite,
                                        Pageable pageable);
           
    // Pop-up prestes a vencer                                    
    @Query("SELECT p FROM PDF p WHERE p.empresa.id = :empresaId AND p.dataValidade BETWEEN CURRENT_DATE AND :dataLimite")
    List<PDF> findPDFsPrestesAVencer(@Param("empresaId") Long empresaId, @Param("dataLimite") LocalDate dataLimite);


}