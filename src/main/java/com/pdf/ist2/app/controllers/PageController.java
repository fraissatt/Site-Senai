package com.pdf.ist2.app.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.pdf.ist2.app.entities.PDF;
import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.PDFRepository;
import com.pdf.ist2.app.repositories.UserRepository;

@Controller
public class PageController {

    @Autowired
    private PDFRepository pdfRepository; 

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "index";  
    }

    @GetMapping("/login")
    public String login() {
        return "login";  
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER')")
    @GetMapping("/menuADM")
    public String adminPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("userRole", role);  // Adiciona role para o front-end

        return "menuADM";  
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER', 'ROLE_CLIENTE')")
    @GetMapping("/visualizar/{empresaId}")
    public String visualizarPagina(@PathVariable Long empresaId, Model model) {
        System.out.println("Empresa ID capturado: " + empresaId);
        model.addAttribute("empresaId", empresaId);

        List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
        model.addAttribute("pdfs", pdfs);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("userRole", role);  // Adiciona role para o front-end

        Long userEmpresaId = getUserEmpresaId(authentication);
        model.addAttribute("userEmpresaId", userEmpresaId);  // Adiciona userEmpresaId para o front-end

        return "visualizar";
    }

    private Long getUserEmpresaId(Authentication authentication) {
        String email = authentication.getName();  // Obtém o e-mail do usuário autenticado
        Optional<Usuario> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().getEmpresa() != null ? user.get().getEmpresa().getId() : null;
    }

    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER', 'ROLE_CLIENTE')")
    // @GetMapping("/visualizar/{empresaId}")
    // public String visualizarPagina(@PathVariable Long empresaId, Model model) {
    //     System.out.println("Empresa ID capturado: " + empresaId);
    //     model.addAttribute("empresaId", empresaId);

    //     List<PDF> pdfs = pdfRepository.findByEmpresaId(empresaId);
    //     model.addAttribute("pdfs", pdfs);

    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     String role = authentication.getAuthorities().iterator().next().getAuthority();
    //     model.addAttribute("userRole", role);  // Adiciona role para o front-end

    //     return "visualizar";  
    // }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER')")
    @GetMapping("/empresa/pagina")
    public String empresasPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("userRole", role);  // Adiciona role para o front-end

        return "empresas"; 
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER')")
    @GetMapping("/users")
    public String usersPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        System.out.println("Role do usuário na tela de Users: " + role);
        model.addAttribute("userRole", role);
        return "users";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER')")
    @GetMapping("/agendamentos")
    public String agendamentosPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        System.out.println("Role do usuário na tela de Agendamentos: " + role);
        model.addAttribute("userRole", role);
        return "agendamentos";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAKEHOLDER', 'ROLE_CLIENTE')")
    @GetMapping("/visualizar/graficos")
    public String graficosPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("userRole", role);  // Adiciona role para o front-end

        return "graficos";
    }
    
}
