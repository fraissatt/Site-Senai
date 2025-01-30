package com.pdf.ist2.app.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pdf.ist2.app.dto.MonthlyReportDTO;
import com.pdf.ist2.app.dto.RelatorioStatusDTO;
import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.repositories.PDFRepository;

@Service
public class PDFService {

    @Autowired
    private PDFRepository pdfRepository;

    public List<PDF> getPDFsByEmpresaId(Long empresaId) {
        return pdfRepository.findByEmpresaId(empresaId);
    }

    // Método para retornar PDFs com base no status
    public List<PDF> getPDFsByEmpresaIdAndStatus(Long empresaId, String status) {
        // Busca todos os PDFs da empresa
        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
        LocalDate currentDate = LocalDate.now();

        // Filtra os PDFs com base no status e na data de validade
        return pdfs.stream().filter(pdf -> {
            LocalDate validade = pdf.getDataValidade();
            if (validade == null) return false;

            long daysUntilExpiration = validade.toEpochDay() - currentDate.toEpochDay();

            return switch (status.toLowerCase()) {
                case "ativo" -> daysUntilExpiration > 30;
                case "prestes a vencer" -> daysUntilExpiration > 0 && daysUntilExpiration <= 30;
                case "expirado" -> daysUntilExpiration <= 0;
                default -> false;
            };
            
        }).collect(Collectors.toList());
    }

    public PDF atualizarEstado(Long pdfId, String novoEstado) {
        PDF pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("PDF não encontrado com o ID: " + pdfId));
        pdf.setEstado(novoEstado);
        return pdfRepository.save(pdf);
    }

    // Método para atualizar a natureza de um PDF
    public PDF atualizarNatureza(Long pdfId, String novaNatureza) {
        PDF pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("PDF não encontrado com o ID: " + pdfId));
        pdf.setNatureza(novaNatureza);
        return pdfRepository.save(pdf);
    }

    public List<RelatorioStatusDTO> getRelatorioStatusPorEmpresa(Long empresaId) {
        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
    
        // Agrupa os PDFs por estado e conta a quantidade de cada estado
        Map<String, Long> statusCount = pdfs.stream()
            .collect(Collectors.groupingBy(PDF::getEstado, Collectors.counting()));
    
        // Converte o Map para uma lista de RelatorioStatusDTO
        return statusCount.entrySet().stream()
            .map(entry -> new RelatorioStatusDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }    

    public Long getQuantidadeRelatoriosPorEmpresa(Long empresaId) {
        return pdfRepository.countByEmpresaId(empresaId);
    }

    public List<MonthlyReportDTO> getRelatoriosPorMes(Long empresaId) {
        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
    
        // Agrupa os PDFs por mês de validade
        Map<YearMonth, Long> pdfsPorMes = pdfs.stream()
            .filter(pdf -> pdf.getDataValidade() != null)
            .collect(Collectors.groupingBy(
                pdf -> YearMonth.from(pdf.getDataValidade()), 
                Collectors.counting()
            ));
    
        // Converte o Map para uma lista de MonthlyReportDTO
        return pdfsPorMes.entrySet().stream()
            .map(entry -> new MonthlyReportDTO(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }
    
    public Page<PDF> buscarPDFsComFiltros(Long empresaId, String status, String estado, String natureza, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate limite = LocalDate.now().plusDays(30);
        status = (status != null && !status.trim().isEmpty()) ? status.toLowerCase() : null;
        estado = (estado != null && !estado.trim().isEmpty()) ? estado : null;
        natureza = (natureza != null && !natureza.trim().isEmpty()) ? natureza : null;
    
        return pdfRepository.findByEmpresaIdAndFiltros(empresaId, status, estado, natureza, limite, pageable);
    }


    
    public List<PDF> buscarPDFsComFiltrosEArquivoSemPaginacao(Long empresaId, String status, String estado, String natureza, String fileName) {
        LocalDate limite = LocalDate.now().plusDays(30);
    
        // Normalize os valores para evitar problemas com strings vazias
        status = (status != null && !status.trim().isEmpty()) ? status.toLowerCase() : null;
        estado = (estado != null && !estado.trim().isEmpty()) ? estado.toLowerCase() : null;
        natureza = (natureza != null && !natureza.trim().isEmpty()) ? natureza.toLowerCase() : null;
        fileName = (fileName != null && !fileName.trim().isEmpty()) ? fileName.toLowerCase() : null;
    
        return pdfRepository.findByEmpresaIdAndFiltrosComNomeSemPaginacao(empresaId, status, estado, natureza, fileName, limite);
    }

    public List<PDF> buscarPDFsPrestesAVencer(Long empresaId) {
        LocalDate dataLimite = LocalDate.now().plusDays(30);
        return pdfRepository.findPDFsPrestesAVencer(empresaId, dataLimite);
    }

        
}