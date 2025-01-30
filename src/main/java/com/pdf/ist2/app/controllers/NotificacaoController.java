package com.pdf.ist2.app.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.services.NotificacaoService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/notificacao")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarNotificacoes(@RequestBody List<PDF> pdfsFiltrados) {
        Map<String, Object> resultado = new HashMap<>();
        int sucesso = 0, falha = 0;

        for (PDF pdf : pdfsFiltrados) {
            Empresa empresa = pdf.getEmpresa();
            if (empresa != null) {
                try {
                    notificacaoService.enviarNotificacaoParaEmpresa(empresa, pdf);
                    sucesso++;
                } catch (MessagingException e) {
                    falha++;
                    System.err.println("Erro ao enviar notificação para empresa: " + empresa.getNomeEmpresa());
                    e.printStackTrace();
                }
            }
        }

        resultado.put("notificacoesEnviadas", sucesso);
        resultado.put("falhas", falha);
        return ResponseEntity.ok(resultado);
    }


}
