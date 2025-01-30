package com.pdf.ist2.app.dto;

import java.time.YearMonth;

public class MonthlyReportDTO {
    private String mes;
    private int quantidade;

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public MonthlyReportDTO(YearMonth mes, int quantidade) {
        this.mes = mes.toString(); // Converte para "YYYY-MM" formato
        this.quantidade = quantidade;
    }

}
