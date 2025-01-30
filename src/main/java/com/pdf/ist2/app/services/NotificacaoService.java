package com.pdf.ist2.app.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.pdf.ist2.app.entities.ClienteNotificacaoEntity;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.repositories.NotificacaoRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class NotificacaoService {

private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private EmpresaService empresaService; // Um serviço para obter todas as empresas

    @Scheduled(fixedRate = 60000) 
    public void enviarNotificacoesAutomatizadas() {
        logger.info("Iniciando envio de notificações automatizadas.");
        LocalDate hoje = LocalDate.now();
    
        List<Empresa> empresas = empresaService.listarTodasEmpresas();
    
        for (Empresa empresa : empresas) {
            if (empresa.getEmail() == null || empresa.getEmail().isEmpty()) continue;
    
            List<PDF> pdfsPrestesAVencer = pdfService.getPDFsByEmpresaIdAndStatus(empresa.getId(), "prestes a vencer");
    
            for (PDF pdf : pdfsPrestesAVencer) {
                long diasParaVencer = ChronoUnit.DAYS.between(hoje, pdf.getDataValidade());
    
                if (diasParaVencer == 30 || diasParaVencer == 15 || (diasParaVencer <= 5 && diasParaVencer > 0)) {
                    if (!notificacaoJaEnviada(pdf, "E-mail")) {
                        try {
                            enviarNotificacaoParaEmpresa(empresa, pdf);
                            logger.info("Notificação enviada para empresa: {} sobre PDF: {}, dias para vencer: {}",
                                    empresa.getNomeEmpresa(), pdf.getFileName(), diasParaVencer);
                        } catch (Exception e) {
                            logger.error("Erro ao enviar notificação para empresa: {}", empresa.getId(), e);
                        }
                    }
                }
            }
        }
    
        logger.info("Envio de notificações automatizadas concluído.");
    }
    
    private boolean notificacaoJaEnviada(PDF pdf, String tipoNotificacao) {
        return notificacaoRepository.existsByPdfAndTipoNotificacao(pdf, tipoNotificacao);
    }
    

    public void enviarNotificacaoParaEmpresa(Empresa empresa, PDF pdf) throws MessagingException {
        String destinatario = empresa.getEmail();
        String assunto = "Aviso: Relatório prestes a vencer";
        Map<String, Object> dadosEmail = new HashMap<>();
        dadosEmail.put("nomeEmpresa", empresa.getNomeEmpresa());
        dadosEmail.put("nomePDF", pdf.getFileName());
        dadosEmail.put("dataValidade", pdf.getDataValidade().toString());

        Context context = new Context();
        context.setVariables(dadosEmail);
        String corpoEmail = templateEngine.process("email-template", context);

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true);
        helper.setFrom("joaovitor@fieg.com.br");
        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(corpoEmail, true);

        mailSender.send(mensagem);
        logger.info("E-mail enviado para empresa: {} sobre o PDF: {}", empresa.getNomeEmpresa(), pdf.getFileName());

        registrarNotificacao(pdf, "E-mail", "Enviada");
    }
    
    private void registrarNotificacao(PDF pdf, String tipoNotificacao, String status) {
        ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
        notificacao.setEmpresa(pdf.getEmpresa());
        notificacao.setPdf(pdf);
        notificacao.setDataEnvio(LocalDateTime.now());
        notificacao.setTipoNotificacao(tipoNotificacao);
        notificacao.setStatus(status);
        notificacaoRepository.save(notificacao);
    }
    
}
