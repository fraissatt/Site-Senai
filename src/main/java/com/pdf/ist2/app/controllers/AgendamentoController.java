package com.pdf.ist2.app.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pdf.ist2.app.dto.AgendamentoDTO;
import com.pdf.ist2.app.entities.Agendamento;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.UserRepository;
import com.pdf.ist2.app.services.AgendamentoService;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;
    
    @Autowired
    private UserRepository userRepository;

    // Endpoint para listar agendamentos disponíveis
    @GetMapping("/disponiveis")
    public ResponseEntity<List<String>> listarAgendamentosDisponiveis() {
        List<String> agendamentosDisponiveis = agendamentoService.listarAgendamentosDisponiveis();
        return ResponseEntity.ok(agendamentosDisponiveis);
    }

    @PostMapping("/reservar")
    public ResponseEntity<String> reservarAgendamento(
            @RequestParam LocalDate data,
            @RequestParam LocalTime horario,
            Authentication authentication) {

        // Obter o usuário logado
        String usuarioLogado = authentication.getName();
        Usuario usuario = userRepository.findByEmail(usuarioLogado)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Obter a empresa associada ao usuário
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            System.out.println("Erro: O usuário " + usuario.getEmail() + " não possui uma empresa associada.");
            throw new RuntimeException("Nenhuma empresa associada ao usuário.");
        } else {
            System.out.println("Empresa associada ao usuário: " + empresa.getNomeEmpresa());
        }

        try {
            // Tentativa de reservar o agendamento
            Agendamento agendamento = agendamentoService.reservarAgendamento(data, horario, empresa);
            System.out.println("Agendamento criado: " + agendamento);

            // Retorna mensagem de sucesso
            return ResponseEntity.ok("Agendamento reservado com sucesso.");

        } catch (RuntimeException e) {
            // Lida com o erro de horário indisponível
            if (e.getMessage().contains("Horário indisponível")) {
                System.out.println("Erro: Horário indisponível para a data " + data + " e horário " + horario);
                // Retorna uma mensagem de erro amigável com status de conflito
                return ResponseEntity.status(HttpStatus.CONFLICT).body("| Horário indisponível |");
            }
            // Lança qualquer outro tipo de erro
            throw e;
        }
    }

    // Endpoint para listar agendamentos por empresa
    @GetMapping("/meus-agendamentos")
    public ResponseEntity<List<AgendamentoDTO>> listarAgendamentosPorEmpresa(Authentication authentication) {
        // Obter o usuário logado
        String emailUsuario = authentication.getName();
        Usuario usuario = userRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Obter a empresa associada ao usuário pelo ID
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new RuntimeException("Nenhuma empresa associada ao usuário.");
        }

        // Listar agendamentos pela empresa associada
        List<AgendamentoDTO> agendamentos = agendamentoService
            .listarAgendamentosPorEmpresa(empresa.getId())
            .stream()
            .map(agendamento -> new AgendamentoDTO(
                agendamento.getId(),
                agendamento.getDataHora(),
                agendamento.getStatus(),
                agendamento.getNomeEmpresa()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/admin/agendamentos")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AgendamentoDTO>> listarTodosAgendamentos(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long empresaId) {
        List<Agendamento> agendamentos = agendamentoService.listarAgendamentosComFiltros(status, empresaId);
        List<AgendamentoDTO> agendamentosDTO = agendamentos.stream()
            .map(agendamento -> new AgendamentoDTO(
                agendamento.getId(),
                agendamento.getDataHora(),
                agendamento.getStatus(),
                agendamento.getEmpresa().getNomeEmpresa()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(agendamentosDTO);
    }


    @PutMapping("/admin/agendamentos/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> atualizarStatusAgendamento(
        @PathVariable Long id, 
        @RequestParam String status) {
        
        try {
            agendamentoService.atualizarStatusAgendamento(id, status);
            return ResponseEntity.ok("Status atualizado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o status: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/agendamentos/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> excluirAgendamento(@PathVariable Long id) {
        try {
            agendamentoService.excluirAgendamento(id);
            return ResponseEntity.ok("Agendamento excluído com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o agendamento: " + e.getMessage());
        }
    }

    @GetMapping("/datas-indisponiveis")
    public ResponseEntity<List<LocalDate>> getDatasIndisponiveis() {
        List<LocalDate> datas = agendamentoService.getDatasCompletamenteReservadas();
        return ResponseEntity.ok(datas);
    }

    @GetMapping("/horarios-indisponiveis")
    public ResponseEntity<List<String>> getHorariosIndisponiveis(@RequestParam String data) {
        // Log para verificar a data recebida no endpoint
        System.out.println("Data recebida no endpoint: " + data);

        List<String> horarios = agendamentoService.getHorariosIndisponiveis(data);

        // Log para verificar os horários retornados
        System.out.println("Horários indisponíveis enviados ao front-end: " + horarios);

        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/empresas")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Empresa>> listarEmpresas() {
        List<Empresa> empresas = agendamentoService.listarEmpresas();
        return ResponseEntity.ok(empresas);
    }


}
