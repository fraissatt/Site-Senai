package com.pdf.ist2.app.dto;

import java.time.LocalDateTime;

public class AgendamentoDTO {
    private Long id;
    private LocalDateTime dataHora;
    private String status;
    private String nomeEmpresa;

    public AgendamentoDTO(Long id, LocalDateTime dataHora, String status, String nomeEmpresa) {
        this.id = id;
        this.dataHora = dataHora;
        this.status = status;
        this.nomeEmpresa = nomeEmpresa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }
}
