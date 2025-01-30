package com.pdf.ist2.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.pdf.ist2.app.dto.MonthlyReportDTO;
import com.pdf.ist2.app.dto.RelatorioStatusDTO;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.repositories.EmpresaRepository;
import com.pdf.ist2.app.repositories.PDFRepository;
import com.pdf.ist2.app.services.NotificacaoService;
import com.pdf.ist2.app.services.PDFService;
    

@Controller
@RequestMapping("/api/pdfs")
public class PdfController {

    @Autowired
    private NotificacaoService notificacaoService;  

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private EmpresaRepository empresaRepository;

    private final String uploadDirectory = "/PDFs/upload/diretorio";

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/uploadAdmin/{empresaId}")
    public ResponseEntity<?> uploadPdf(@PathVariable Long empresaId, @RequestParam("file") MultipartFile file) {
        // Lógica para fazer o upload de PDFs
        return ResponseEntity.ok("PDF enviado com sucesso!");
    }

    @PostMapping("/upload/{empresaId}")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file, @PathVariable Long empresaId) {
        try {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            // Diretório de upload, usando apenas o nome da empresa, sem "upload/diretorio"
            String empresaDir = "C:/PDFs/" + empresa.getNomeEmpresa();
            Path empresaPath = Paths.get(empresaDir);

            // Cria o diretório para a empresa, caso não exista
            if (!Files.exists(empresaPath)) {
                Files.createDirectories(empresaPath);
            }

            // Caminho do arquivo
            Path filePath = construirCaminhoPdf(empresa.getNomeEmpresa(), file.getOriginalFilename());

            // Verifica se o arquivo já existe
            if (Files.exists(filePath)) {
                // Se o arquivo existir, retorna o status 409 (Conflito) e não permite o upload
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Um arquivo com o mesmo nome já existe.\"}");
            }

            // Se o arquivo não existe, realiza o upload
            Files.write(filePath, file.getBytes());

            // Ajusta o caminho para ser compatível com URL pública (sem "C:/PDFs/")
            String publicUrlPath = "/PDFs/" + empresa.getNomeEmpresa() + "/" + file.getOriginalFilename();

            // Armazenar o caminho no banco de dados
            PDF pdf = new PDF();
            pdf.setFileName(file.getOriginalFilename());
            pdf.setFilePath(publicUrlPath);  // Usando o caminho simplificado
            pdf.setEmpresa(empresa);
            pdf.setDataValidade(null);  // Definir data de validade posteriormente
            pdfRepository.save(pdf);

            return ResponseEntity.ok("{\"message\": \"PDF salvo com sucesso!\"}");
        } catch (Exception e) {
            return new ResponseEntity<>("{\"error\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/empresa/{empresaId}/buscar")
    public ResponseEntity<Page<PDF>> buscarPdfsPorNome(
            @PathVariable Long empresaId,
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String natureza,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PDF> pdfs;

        // Se nenhum filtro for fornecido, busca apenas pelo ID da empresa
        if (nome.isEmpty() && status == null && estado == null && natureza == null) {
            pdfs = pdfRepository.findByEmpresaId(empresaId, pageable);

        // Se o nome for fornecido, busca pelo nome do arquivo
        } else if (!nome.isEmpty()) {
            pdfs = pdfRepository.findByEmpresaIdAndFileNameContainingIgnoreCase(empresaId, nome, pageable);

        // Se outros filtros forem fornecidos, usa o método com filtros adicionais
        } else {
            pdfs = pdfService.buscarPDFsComFiltros(empresaId, status, estado, natureza, page, size);
        }

        return ResponseEntity.ok(pdfs);
    }


    @GetMapping("/empresa/{empresaId}/filtrar-sem-paginacao")
    @ResponseBody
    public List<PDF> buscarPDFsSemPaginacao(
            @PathVariable Long empresaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String natureza,
            @RequestParam(required = false) String fileName) {

        // Logs para depuração
        System.out.println("==== Requisição recebida no Controller ====");
        System.out.println("Empresa ID: " + empresaId);
        System.out.println("Status: " + status);
        System.out.println("Estado: " + estado);
        System.out.println("Natureza: " + natureza);
        System.out.println("FileName: " + fileName);
        System.out.println("=========================================================");

        return pdfService.buscarPDFsComFiltrosEArquivoSemPaginacao(empresaId, status, estado, natureza, fileName);
    }



    
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/validade")
    public ResponseEntity<String> atualizarValidade(@PathVariable Long id, @RequestBody String dataValidade) {
        try {
            System.out.println("ID do PDF recebido: " + id);
            System.out.println("Data de validade recebida: " + dataValidade);

            Optional<PDF> pdfOptional = pdfRepository.findById(id);
            if (pdfOptional.isPresent()) {
                PDF pdf = pdfOptional.get();
                LocalDate validade = LocalDate.parse(dataValidade);
                System.out.println("Data de validade parseada: " + validade);
                pdf.setDataValidade(validade);
                pdfRepository.save(pdf);
                System.out.println("PDF atualizado com sucesso!");
                return ResponseEntity.ok("Data de validade atualizada com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o stacktrace no log para diagnosticar o problema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar data de validade");
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<String> deletePdf(@PathVariable Long id) {
    try {
        System.out.println("Iniciando o processo de exclusão do PDF com ID: " + id);

        Optional<PDF> pdfOptional = pdfRepository.findById(id);
        if (pdfOptional.isPresent()) {
            PDF pdf = pdfOptional.get();
            System.out.println("PDF encontrado: " + pdf.getFileName() + " | Empresa: " + pdf.getEmpresa().getNomeEmpresa());

            Path excluidosDir = Paths.get("C:/PDFs/excluidos/");
            if (!Files.exists(excluidosDir)) {
                Files.createDirectories(excluidosDir);
                System.out.println("Pasta 'excluidos' criada com sucesso.");
            }

            Path originalPath = Paths.get("C:/PDFs/" + pdf.getEmpresa().getNomeEmpresa() + "/" + pdf.getFileName());
            Path newPath = excluidosDir.resolve(pdf.getFileName());

            // Verifica se o arquivo já existe no diretório de destino
            if (Files.exists(newPath)) {
                System.out.println("Arquivo já existe na pasta 'excluidos'. Renomeando arquivo...");
                String newFileName = System.currentTimeMillis() + "_" + pdf.getFileName();
                newPath = excluidosDir.resolve(newFileName);
                System.out.println("Novo caminho: " + newPath.toString());
            }

            if (!Files.exists(originalPath)) {
                System.err.println("Arquivo original não encontrado: " + originalPath.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo original não encontrado.");
            }

            Files.move(originalPath, newPath);
            System.out.println("Arquivo movido com sucesso para a pasta 'excluidos'.");

            pdf.setFilePath("/PDFs/excluidos/" + newPath.getFileName());
            pdfRepository.save(pdf);
            System.out.println("Caminho do PDF atualizado no banco de dados.");

            pdfRepository.deleteById(id);
            System.out.println("PDF removido do banco de dados com sucesso.");

            return ResponseEntity.ok("PDF movido para 'excluidos' e removido do banco de dados com sucesso.");
        } else {
            System.err.println("PDF não encontrado no banco de dados com ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF não encontrado.");
        }
    } catch (IOException e) {
        System.err.println("Erro ao mover arquivo para 'excluidos': " + e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>("Erro ao mover arquivo para 'excluidos': " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
        System.err.println("Erro inesperado ao excluir PDF: " + e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>("Erro inesperado ao excluir PDF: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}




    // public void deletePdf(Long id) {
    //     Optional<PDF> pdfOptional = pdfRepository.findById(id);
    //     if (pdfOptional.isPresent()) {
    //         PDF pdf = pdfOptional.get();
    //         Path filePath = Paths.get(pdf.getFilePath());
    //         Path destino = Paths.get("C:/PDFs/excluidos/" + pdf.getFileName());

    //         try {
    //             // Se o arquivo de destino já existe, adiciona um sufixo único
    //             if (Files.exists(destino)) {
    //                 destino = gerarNovoNomeArquivo(destino);
    //             }

    //             // Move o arquivo para a pasta "excluídos" com o novo nome, se necessário
    //             Files.move(filePath, destino, StandardCopyOption.REPLACE_EXISTING);

    //             // Exclui o registro do banco de dados
    //             pdfRepository.deleteById(id);
    //         } catch (IOException e) {
    //             throw new RuntimeException("Erro ao mover o arquivo para a pasta 'excluídos': " + e.getMessage(), e);
    //         }
    //     } else {
    //         throw new RuntimeException("PDF não encontrado para exclusão");
    //     }
    // }

    // // Método auxiliar para gerar um novo nome de arquivo com sufixo único
    // private Path gerarNovoNomeArquivo(Path destinoOriginal) {
    //     String nomeArquivo = destinoOriginal.getFileName().toString();
    //     String nomeBase = nomeArquivo.contains(".")
    //         ? nomeArquivo.substring(0, nomeArquivo.lastIndexOf('.'))
    //         : nomeArquivo;
    //     String extensao = nomeArquivo.contains(".")
    //         ? nomeArquivo.substring(nomeArquivo.lastIndexOf('.'))
    //         : "";
        
    //     int contador = 1;
    //     Path novoDestino = destinoOriginal;
    //     while (Files.exists(novoDestino)) {
    //         novoDestino = Paths.get(destinoOriginal.getParent().toString(),
    //             nomeBase + "_" + contador + extensao);
    //         contador++;
    //     }
    //     return novoDestino;
    // }



    // Método adicional no PdfController para deletar todos os PDFs de uma empresa
    public void deletePdfsByEmpresaId(Long empresaId) {
        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
        for (PDF pdf : pdfs) {
            Path filePath = Paths.get(pdf.getFilePath());
            try {
                Files.deleteIfExists(filePath);  // Remove o arquivo do sistema
            } catch (IOException e) {
                // Log e continue para tentar deletar outros arquivos
                System.err.println("Erro ao deletar arquivo PDF: " + pdf.getFilePath() + " - " + e.getMessage());
            }
            // Remove do banco
            pdfRepository.delete(pdf);
        }
    }


    // Método para redirecionar para a página "visualizar" e carregar PDFs de uma empresa específica
    @GetMapping("/visualizar/{empresaId}")
    public String visualizarPdfs(@PathVariable("empresaId") Long empresaId, Model model) {
        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
        model.addAttribute("pdfs", pdfs);  // Adiciona a lista de PDFs ao modelo
        model.addAttribute("empresaId", empresaId);  // Adiciona o ID da empresa ao modelo
        return "visualizar";  // Retorna a página "visualizar.html"
    }

    @GetMapping("/pdfs/{empresa}/{filename}")
    public ResponseEntity<Resource> getPdf(@PathVariable String empresa, @PathVariable String filename) throws IOException {
        // Define o caminho completo para o arquivo, utilizando o mesmo padrão do upload
        Path filePath = construirCaminhoPdf(empresa, filename);
        Resource file = new UrlResource(filePath.toUri());

        // Verifica se o arquivo existe e pode ser lido
        if (file.exists() || file.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } else {
            throw new RuntimeException("Não foi possível ler o arquivo: " + filename);
        }
    }


    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PDF>> getPdfsByEmpresaIdAndStatus(@PathVariable Long empresaId, @RequestParam(required = false) String status) {
        try {
            List<PDF> pdfs = (status == null || status.isEmpty()) 
                                ? pdfService.getPDFsByEmpresaId(empresaId)
                                : pdfService.getPDFsByEmpresaIdAndStatus(empresaId, status);
            return new ResponseEntity<>(pdfs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> atualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String estado = request.get("estado");
            System.out.println("ID do PDF recebido para atualização de estado: " + id);
            System.out.println("Estado recebido: " + estado);

            Optional<PDF> pdfOptional = pdfRepository.findById(id);
            if (pdfOptional.isPresent()) {
                PDF pdf = pdfOptional.get();
                pdf.setEstado(estado);
                pdfRepository.save(pdf);
                System.out.println("Estado atualizado com sucesso!");
                return ResponseEntity.ok("Estado atualizado com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o stacktrace no log para diagnóstico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar estado");
        }
    }

    @PutMapping("/{id}/natureza")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> atualizarNatureza(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String natureza = request.get("natureza");
            System.out.println("ID do PDF recebido para atualização de natureza: " + id);
            System.out.println("Natureza recebida: " + natureza);

            Optional<PDF> pdfOptional = pdfRepository.findById(id);
            if (pdfOptional.isPresent()) {
                PDF pdf = pdfOptional.get();
                pdf.setNatureza(natureza);
                pdfRepository.save(pdf);
                System.out.println("Natureza atualizada com sucesso!");
                return ResponseEntity.ok("Natureza atualizada com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o stacktrace no log para diagnóstico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar natureza");
        }
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    @GetMapping("/status")
    public ResponseEntity<List<RelatorioStatusDTO>> getRelatorioStatusPorEmpresa(@RequestParam Long empresaId) {
        if (empresaId == null || empresaId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<RelatorioStatusDTO> relatorios = pdfService.getRelatorioStatusPorEmpresa(empresaId);
        return ResponseEntity.ok(relatorios);
    }

    @GetMapping("/quantidade")
    public ResponseEntity<Long> getQuantidadeRelatoriosPorEmpresa(@RequestParam Long empresaId) {
        Long quantidade = pdfService.getQuantidadeRelatoriosPorEmpresa(empresaId);
        return ResponseEntity.ok(quantidade);
    }

    @GetMapping("/quantidade-por-mes")
    public ResponseEntity<List<MonthlyReportDTO>> getRelatoriosPorMes(@RequestParam Long empresaId) {
        List<MonthlyReportDTO> relatorios = pdfService.getRelatoriosPorMes(empresaId);
        return ResponseEntity.ok(relatorios);
    }

    public Path construirCaminhoPdf(String empresa, String filename) {
        return Paths.get("C:/PDFs/" + empresa + "/" + filename);
    }    

    @GetMapping("/empresa/{empresaId}/verificar-prestes-a-vencer")
    public ResponseEntity<Boolean> verificarPDFsPrestesAVencer(@PathVariable Long empresaId) {
        List<PDF> pdfsPrestesAVencer = pdfService.buscarPDFsPrestesAVencer(empresaId);
        return ResponseEntity.ok(!pdfsPrestesAVencer.isEmpty());
    }

}