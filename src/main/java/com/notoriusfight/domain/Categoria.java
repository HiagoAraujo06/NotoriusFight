package com.notoriusfight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Artigo> artigos = new ArrayList<>();

    protected Categoria() {
    }

    public Categoria(String nome) {
        this.nome = validarTextoObrigatorio(nome, "Nome da categoria é obrigatório");
        this.status = Status.ATIVO;
    }

    public void adicionarArtigo(Artigo artigo) {
        Objects.requireNonNull(artigo, "Artigo é obrigatório");

        boolean codigoJaUtilizado = artigos.stream()
                .anyMatch(item -> item != artigo
                        && item.getSku().equals(artigo.getSku()));

        if (codigoJaUtilizado) {
            throw new IllegalArgumentException("SKU já utilizado na categoria");
        }

        artigo.associarAo(this);

        if (!artigos.contains(artigo)) {
            artigos.add(artigo);
        }
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public String getNome() {
        return nome;
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public List<Artigo> getArtigos() {
        return List.copyOf(artigos);
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }
}
