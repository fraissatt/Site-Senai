package com.pdf.ist2.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pdf.ist2.app.entities.Empresa;
import com.pdf.ist2.app.entities.Usuario;
import com.pdf.ist2.app.repositories.AgendamentoRepository;
import com.pdf.ist2.app.repositories.EmpresaRepository;
import com.pdf.ist2.app.repositories.UserRepository;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UserRepository userRepository;

    // Salvar uma nova empresa
    public Empresa salvarEmpresa(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    // Buscar todas as empresas
    public List<Empresa> listarTodasEmpresas() {
        return empresaRepository.findAll();
    }

    // Buscar empresa por ID
    public Optional<Empresa> buscarEmpresaPorId(Long id) {
        return empresaRepository.findById(id);
    }

    // Atualizar uma empresa existente
    public Empresa atualizarEmpresa(Long id, Empresa empresaAtualizada) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setNomeEmpresa(empresaAtualizada.getNomeEmpresa());
            empresa.setEmail(empresaAtualizada.getEmail());
            empresa.setCelular(empresaAtualizada.getCelular());
            // Atualize outros campos conforme necessário
            return empresaRepository.save(empresa);
        }).orElseThrow(() -> new RuntimeException("Empresa não encontrada com o ID: " + id));
    }

    // Excluir uma empresa por ID
    @Transactional
    public void excluirEmpresa(Long id) {
        if (empresaRepository.existsById(id)) {
            agendamentoRepository.deleteByEmpresaId(id);
            empresaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Empresa não encontrada com o ID: " + id);
        }
    }

    public String obterEstadoEmpresaUsuario(Authentication authentication) {
        // Obter o e-mail do usuário autenticado
        String emailUsuario = authentication.getName();
    
        // Buscar o usuário no banco de dados
        Optional<Usuario> usuario = userRepository.findByEmail(emailUsuario);
    
        // Verificar se o usuário está associado a uma empresa e retornar o estado
        if (usuario.isPresent() && usuario.get().getEmpresa() != null) {
            return usuario.get().getEmpresa().getEmpresaEstado();
        }
    
        // Retornar "N/C" caso o usuário não esteja associado a nenhuma empresa
        return "N/C";
    }
    
}