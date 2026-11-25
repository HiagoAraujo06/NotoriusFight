package com.notoriusfight.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Categoria {

    private final String nome;
    private Status status;
    private final List<Artigo> artigos = new ArrayList<>();

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
