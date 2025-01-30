package com.pdf.ist2.app.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@JsonIgnoreProperties({"pdfs", "agendamentos"})
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeEmpresa;
    private String email;
    private String celular;
    
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties("empresa") 
    private List<PDF> pdfs = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties("empresa") 
    private List<Agendamento> agendamentos = new ArrayList<>();

    @Column(name = "visivel_stakeholder", nullable = false)
    private boolean visivelStakeholder = false;

    @Column(name = "empresa_estado", nullable = false, length = 2)
    private String empresaEstado = "N/C";

    public Empresa(String nomeEmpresa){
        this.nomeEmpresa = nomeEmpresa;
    }

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

    public List<PDF> getPdfs() {
        return pdfs;
    }
    
    public void setPdfs(List<PDF> pdfs) {
        this.pdfs = pdfs;
    }

    public boolean isVisivelStakeholder() {
        return visivelStakeholder;
    }

    public void setVisivelStakeholder(boolean visivelStakeholder) {
        this.visivelStakeholder = visivelStakeholder;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }
    
    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }    

    public String getEmpresaEstado() {
        return empresaEstado;
    }
    
    public void setEmpresaEstado(String empresaEstado) {
        this.empresaEstado = empresaEstado;
    }

    public Empresa() {
        // Construtor vazio necessário para o JPA
    }
    
}