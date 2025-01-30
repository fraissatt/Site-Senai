package com.pdf.ist2.app.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pdf.ist2.app.entities.Agendamento;
import com.pdf.ist2.app.entities.Empresa;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Buscar agendamentos em um horário específico
    List<Agendamento> findByDataHora(LocalDateTime dataHora);

    // Buscar agendamentos de uma empresa
    List<Agendamento> findByEmpresaId(Long empresaId);

    void deleteByEmpresaId(Long empresaId);

    @Query("SELECT a.dataHora FROM Agendamento a " +
        "WHERE a.status IN ('Pendente', 'Agendado') " +
        "AND CAST(a.dataHora AS date) = :data")
    List<LocalDateTime> findHorariosIndisponiveisByData(@Param("data") LocalDate data);


    @Query("SELECT a.dataHora FROM Agendamento a WHERE a.status IN ('PENDENTE', 'AGENDADO') " +
        "GROUP BY a.dataHora " +
        "HAVING COUNT(a) = :maxHorariosPorDia")
    List<LocalDate> findDatasCompletamenteReservadas(@Param("maxHorariosPorDia") int maxHorariosPorDia);

    @Repository
    public interface EmpresaRepository extends JpaRepository<Empresa, Long> {}

    List<Agendamento> findByStatus(String status);
    List<Agendamento> findByStatusAndEmpresaId(String status, Long empresaId);


}
