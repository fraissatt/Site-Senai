package com.pdf.ist2.app.dto;

public class EmpresaDTO {
    private Long id;
    private String nomeEmpresa;
    private String email;
    private String celular;
    private String empresaEstado;
    private boolean visivelStakeholder;

    // Construtor que recebe uma entidade Empresa
    public EmpresaDTO(Long id, String nomeEmpresa, String email, String celular, String empresaEstado, boolean visivelStakeholder) {
        this.id = id;
        this.nomeEmpresa = nomeEmpresa;
        this.email = email;
        this.celular = celular;
        this.empresaEstado = empresaEstado;
        this.visivelStakeholder = visivelStakeholder;
    }

    public EmpresaDTO() {
        // Construtor vazio para frameworks
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmpresaEstado() {
        return empresaEstado;
    }

    public void setEmpresaEstado(String empresaEstado) {
        this.empresaEstado = empresaEstado;
    }

    public boolean isVisivelStakeholder() {
        return visivelStakeholder;
    }

    public void setVisivelStakeholder(boolean visivelStakeholder) {
        this.visivelStakeholder = visivelStakeholder;
    }
}
