package com.pdf.ist2.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pdf.ist2.app.entities.ClienteNotificacaoEntity;
import com.pdf.ist2.app.entities.PDF;

@Repository
public interface NotificacaoRepository extends JpaRepository<ClienteNotificacaoEntity, Long> {

    // Verifica se já existe uma notificação enviada para o PDF específico e tipo de notificação
    @Query("SELECT COUNT(n) > 0 FROM ClienteNotificacaoEntity n WHERE n.pdf = :pdf AND n.tipoNotificacao = :tipoNotificacao")
    boolean existsByPdfAndTipoNotificacao(PDF pdf, String tipoNotificacao);

    // Opcional: Buscar a última notificação enviada para um PDF
    @Query("SELECT n FROM ClienteNotificacaoEntity n WHERE n.pdf = :pdf ORDER BY n.dataEnvio DESC")
    Optional<ClienteNotificacaoEntity> findLastByPdf(PDF pdf);
}
