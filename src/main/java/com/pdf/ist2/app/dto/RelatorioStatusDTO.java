package com.pdf.ist2.app.dto;

public class RelatorioStatusDTO {
    private String estado;
    private Long quantidade;

    // Construtor que o Hibernate precisa
    public RelatorioStatusDTO(String estado, Long quantidade) {
        this.estado = estado;
        this.quantidade = quantidade;
    }

    // Getters e Setters
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
