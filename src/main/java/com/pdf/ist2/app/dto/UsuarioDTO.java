package com.pdf.ist2.app.dto;

public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private Long empresaId;
    private String empresa;  
    private String role;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() { 
        return nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getSenha() { 
        return senha; 
    }
    public void setSenha(String senha) { 
        this.senha = senha; 
    }

    public Long getEmpresaId() { 
        return empresaId; 
    }
    public void setEmpresaId(Long empresaId) { 
        this.empresaId = empresaId; 
    }

    public String getEmpresa() { 
        return empresa; 
    }
    public void setEmpresa(String empresa) { 
        this.empresa = empresa; 
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
