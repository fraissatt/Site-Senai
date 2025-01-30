package com.pdf.ist2.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pdf.ist2.app.dto.EmpresaDTO;
import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.repositories.EmpresaRepository;
import com.pdf.ist2.app.services.EmpresaService;
import com.pdf.ist2.app.services.PDFService;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private PdfController pdfController; 

    @Autowired
    private EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<Page<EmpresaDTO>> listarEmpresas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeEmpresa") String sortBy) {

        // Obtenha o papel do usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifique se o usuário tem o papel de "ROLE_STAKEHOLDER"
        boolean isStakeholder = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_STAKEHOLDER"));

        // Configuração de paginação
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        Page<Empresa> empresas;

        if (isStakeholder) {
            // Obter o estado da empresa associada ao usuário stakeholder
            String estadoUsuarioStakeholder = empresaService.obterEstadoEmpresaUsuario(authentication);

            // Log do estado do stakeholder para depuração
            logger.info("Estado do usuário stakeholder: {}", estadoUsuarioStakeholder);

            // Filtrar as empresas visíveis para stakeholders e do mesmo estado
            empresas = empresaRepository.findByEmpresaEstadoAndVisivelStakeholder(estadoUsuarioStakeholder, true, pageable);

            // Log das empresas visíveis
            logger.info("Empresas visíveis no estado {}: {}", estadoUsuarioStakeholder, empresas.getContent());
        } else {
            empresas = empresaRepository.findAll(pageable);
        }

        // Converter Page<Empresa> para Page<EmpresaDTO>
        Page<EmpresaDTO> empresaDTOs = empresas.map(empresa ->
            new EmpresaDTO(
                empresa.getId(),
                empresa.getNomeEmpresa(),
                empresa.getEmail(),
                empresa.getCelular(),
                empresa.getEmpresaEstado(),
                empresa.isVisivelStakeholder()
            )
        );

        return ResponseEntity.ok(empresaDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> getEmpresaById(@PathVariable("id") Long id) {
        Optional<Empresa> empresa = empresaRepository.findById(id);
        return empresa.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{empresaId}/pdfs")
    public List<PDF> listarPDFsPorEmpresa(@PathVariable Long empresaId) {
        return pdfService.getPDFsByEmpresaId(empresaId);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createEmpresa(@RequestBody Empresa empresa) {
        // Verificar duplicidade de nome
        if (empresaRepository.findByNomeEmpresa(empresa.getNomeEmpresa()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Empresa com este nome já cadastrada!");
        }

        // Verificar duplicidade de e-mail
        if (empresaRepository.findByEmail(empresa.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Empresa com este e-mail já cadastrada!");
        }

        // Garante o valor padrão do estado, caso não seja enviado
        if (empresa.getEmpresaEstado() == null || empresa.getEmpresaEstado().isEmpty()) {
            empresa.setEmpresaEstado("N/C");
        }

        // Cria a empresa no banco de dados
        Empresa novaEmpresa = empresaRepository.save(empresa);

        // Criar diretório para a nova empresa no sistema de arquivos
        Path empresaDir = Paths.get("C:/PDFs/" + novaEmpresa.getNomeEmpresa());
        try {
            if (!Files.exists(empresaDir)) {
                Files.createDirectories(empresaDir);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar diretório da empresa: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(novaEmpresa);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmpresa(@PathVariable Long id, @RequestBody Empresa updatedEmpresa) {
        return empresaRepository.findById(id).map(existingEmpresa -> {
            // Verificar se o e-mail já pertence a outra empresa
            Optional<Empresa> empresaComMesmoEmail = empresaRepository.findByEmail(updatedEmpresa.getEmail());
            if (empresaComMesmoEmail.isPresent() && !empresaComMesmoEmail.get().getId().equals(existingEmpresa.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já está em uso por outra empresa!");
            }

            // Atualizar os dados da empresa
            existingEmpresa.setNomeEmpresa(updatedEmpresa.getNomeEmpresa());
            existingEmpresa.setEmail(updatedEmpresa.getEmail());
            existingEmpresa.setCelular(updatedEmpresa.getCelular());
            existingEmpresa.setVisivelStakeholder(updatedEmpresa.isVisivelStakeholder());
            existingEmpresa.setEmpresaEstado(updatedEmpresa.getEmpresaEstado());

            // Salvar e retornar a empresa atualizada
            Empresa updated = empresaRepository.save(existingEmpresa);
            return ResponseEntity.ok(updated);
        }).orElseGet(() -> {
            // Caso a empresa não exista, criar uma nova com o ID especificado
            updatedEmpresa.setId(id);
            Empresa created = empresaRepository.save(updatedEmpresa);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmpresa(@PathVariable Long id) {
        try {
            // Excluir PDFs associados à empresa usando PdfController
            pdfController.deletePdfsByEmpresaId(id);

            // Buscar a empresa antes de excluí-la do banco de dados para obter o nome
            Optional<Empresa> empresaOptional = empresaRepository.findById(id);
            if (empresaOptional.isPresent()) {
                Empresa empresa = empresaOptional.get();

                // Excluir empresa do banco de dados
                empresaRepository.deleteById(id);

                // Caminho da pasta da empresa a ser movida
                Path empresaDir = Paths.get("C:/PDFs/" + empresa.getNomeEmpresa());
                Path excluidosDir = Paths.get("C:/PDFs/excluidos/" + empresa.getNomeEmpresa());

                // Verifica se a pasta da empresa existe
                if (Files.exists(empresaDir)) {
                    try {
                        // Cria o diretório "excluidos" se não existir
                        if (!Files.exists(excluidosDir.getParent())) {
                            Files.createDirectories(excluidosDir.getParent());
                        }

                        // Move a pasta da empresa para o diretório "excluidos", incluindo todos os arquivos e subdiretórios
                        Files.walk(empresaDir)
                            .forEach(source -> {
                                try {
                                    Path destination = excluidosDir.resolve(empresaDir.relativize(source));
                                    Files.move(source, destination);
                                } catch (IOException e) {
                                    throw new RuntimeException("Erro ao mover arquivo ou diretório: " + source, e);
                                }
                            });

                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro ao mover o diretório da empresa para 'excluidos': " + e.getMessage());
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa não encontrada.");
            }

            return ResponseEntity.ok("Empresa e PDFs movidos para 'excluidos' com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir a empresa e mover os arquivos: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Empresa>> buscarEmpresas(@RequestParam String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<Empresa> empresas = empresaRepository.findByNomeEmpresaContainingIgnoreCase(termo);
        return ResponseEntity.ok(empresas);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Empresa>> listarTodasEmpresas() {
        List<Empresa> empresas = empresaRepository.findAll();
        return ResponseEntity.ok(empresas);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Empresa>> listarPorEstado(@PathVariable String estado) {
        List<Empresa> empresas = empresaRepository.findByEmpresaEstado(estado);
        return ResponseEntity.ok(empresas);
    }

}
