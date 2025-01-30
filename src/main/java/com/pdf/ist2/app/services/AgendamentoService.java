package com.pdf.ist2.app.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pdf.ist2.app.dto.AgendamentoDTO;
import com.pdf.ist2.app.entities.Agendamento;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.repositories.AgendamentoRepository;
import com.pdf.ist2.app.repositories.EmpresaRepository;

@Service
public class AgendamentoService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    // Verificar se a data e horário estão disponíveis
    public boolean verificarDisponibilidade(LocalDateTime dataHora) {
        List<Agendamento> agendamentos = agendamentoRepository.findByDataHora(dataHora);
        return agendamentos.isEmpty();
    }

    // Reservar data e horário
    public Agendamento reservarAgendamento(LocalDate data, LocalTime horario, Empresa empresa) {
        LocalDateTime dataHora = LocalDateTime.of(data, horario);

        // Verificar se o horário está disponível
        if (verificarDisponibilidade(dataHora)) {
            // Criar novo agendamento
            Agendamento agendamento = new Agendamento();
            agendamento.setDataHora(dataHora);
            agendamento.setEmpresa(empresa);
            agendamento.setStatus("Pendente");

            // Salvar e retornar o agendamento
            return agendamentoRepository.save(agendamento);
        } else {
            throw new RuntimeException("Horário indisponível");
        }
    }

    // Listar agendamentos de uma empresa
    public List<AgendamentoDTO> listarAgendamentosPorEmpresa(Long empresaId) {
        List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaId(empresaId);
        return agendamentos.stream()
            .map(agendamento -> new AgendamentoDTO(
                agendamento.getId(),
                agendamento.getDataHora(),
                agendamento.getStatus(),
                agendamento.getNomeEmpresa() 
            ))
            .collect(Collectors.toList());
    }


    // Listar horários disponíveis
    public List<String> listarAgendamentosDisponiveis() {
        List<String> horariosDisponiveis = new ArrayList<>();

        // Exemplo de horas disponíveis
        LocalTime[] horarios = {
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            LocalTime.of(13, 0),
            LocalTime.of(15, 0),
            LocalTime.of(17, 0)
        };

        // Exemplo: listar próximos 30 dias úteis (pular sábados e domingos)
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate data = hoje.plusDays(i);
            if (data.getDayOfWeek().getValue() < 6) {  // Exclui sábado e domingo
                for (LocalTime horario : horarios) {
                    LocalDateTime dataHora = LocalDateTime.of(data, horario);
                    if (verificarDisponibilidade(dataHora)) {
                        horariosDisponiveis.add("Data: " + data + " Horário: " + horario);
                    }
                }
            }
        }

        return horariosDisponiveis;
    }

    public List<AgendamentoDTO> listarTodosAgendamentos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        return agendamentos.stream()
            .map(agendamento -> new AgendamentoDTO(
                agendamento.getId(), 
                agendamento.getDataHora(), 
                agendamento.getStatus(),
                agendamento.getNomeEmpresa() 
            ))
            .collect(Collectors.toList());
    }

    public void atualizarStatusAgendamento(Long id, String status) {
        Agendamento agendamento = agendamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
        
        if (!status.equalsIgnoreCase("Pendente") && 
            !status.equalsIgnoreCase("Agendado") && 
            !status.equalsIgnoreCase("Negado")) {
            throw new RuntimeException("Status inválido.");
        }
        
        agendamento.setStatus(status);
        agendamentoRepository.save(agendamento);
    }    

    public void excluirAgendamento(Long id) {
        agendamentoRepository.deleteById(id);
    }    
    
    public List<LocalDate> getDatasCompletamenteReservadas() {
        int maxHorariosPorDia = 5; // Limite de horários por dia
        return agendamentoRepository.findDatasCompletamenteReservadas(maxHorariosPorDia);
    }
    

    public List<String> getHorariosIndisponiveis(String data) {
        LocalDate localDate = LocalDate.parse(data); // Converte a String da data para LocalDate
    
        // Debug para verificar a data
        System.out.println("Data recebida no serviço: " + localDate);
    
        List<LocalDateTime> horariosIndisponiveis = agendamentoRepository.findHorariosIndisponiveisByData(localDate);
    
        // Debug para verificar os resultados do banco
        System.out.println("Horários indisponíveis retornados do banco: " + horariosIndisponiveis);
    
        return horariosIndisponiveis.stream()
                .map(horario -> horario.toLocalTime().toString()) // Converte para String no formato "HH:mm"
                .collect(Collectors.toList());
    }
    
    public List<Empresa> listarEmpresas() {
        return empresaRepository.findAll(); // Corrigido para usar o método correto do repository
    }
       

    public List<Agendamento> listarAgendamentosComFiltros(String status, Long empresaId) {
        if (status != null && empresaId != null) {
            return agendamentoRepository.findByStatusAndEmpresaId(status, empresaId);
        } else if (status != null) {
            return agendamentoRepository.findByStatus(status);
        } else if (empresaId != null) {
            return agendamentoRepository.findByEmpresaId(empresaId);
        }
        return agendamentoRepository.findAll();
    }    
    

}
