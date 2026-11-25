package com.notoriusfight.application;

import com.notoriusfight.domain.Pessoa;
import com.notoriusfight.domain.PerfilAcesso;
import com.notoriusfight.domain.Status;
import com.notoriusfight.domain.StatusContaAcesso;
import com.notoriusfight.repository.ContaAcessoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import com.notoriusfight.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class PessoaService {
    private final PessoaRepository repository;

    private final ContaAcessoRepository contaRepository;

    public PessoaService(
            PessoaRepository repository,
            ContaAcessoRepository contaRepository) {
        this.repository = repository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public Pessoa cadastrar(Pessoa pessoa) {
        validarUnicidade(pessoa.getCpf(), pessoa.getEmail(), null);
        return repository.save(pessoa);
    }

    @Transactional(readOnly = true)
    public Pessoa buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional
    public Pessoa alterar(Long id, String nome, String email, String cpf) {
        Pessoa pessoa = buscarEntidade(id);
        validarUnicidade(cpf, email, id);
        pessoa.alterarDados(nome, email, cpf);
        return pessoa;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Pessoa alterarStatus(Long id, Status status) {
        if (status == null) throw new IllegalArgumentException("Status é obrigatório");
        Pessoa pessoa = buscarEntidade(id);
        if (status == Status.INATIVO) exigirOutroAdministrador(id);
        if (status == Status.ATIVO) pessoa.ativar(); else pessoa.inativar();
        return pessoa;
    }

    @Transactional(readOnly = true)
    public Page<Pessoa> pesquisar(String nome, String cpf, Status status, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "nome", "email", "cpf", "dataCadastro", "status"));
        Specification<Pessoa> filtros = (root, query, cb) -> cb.conjunction();
        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), trecho));
        }
        if (cpf != null && !cpf.isBlank()) {
            filtros = filtros.and((root, query, cb) -> cb.equal(root.get("cpf"), cpf.trim()));
        }
        if (status != null) {
            filtros = filtros.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return repository.findAll(filtros, pageable);
    }

    private void exigirOutroAdministrador(Long pessoaId) {
        boolean administradorAtivo = contaRepository.findByPessoaId(pessoaId)
                .filter(conta -> conta.podeAutenticar()
                        && conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR))
                .isPresent();
        if (administradorAtivo && contaRepository.countByPerfilAndStatus(
                PerfilAcesso.ADMINISTRADOR, StatusContaAcesso.ATIVA) <= 1) {
            throw new IllegalArgumentException(
                    "O sistema deve manter ao menos um administrador ativo");
        }
    }

    private void validarUnicidade(String cpf, String email, Long idAtual) {
        boolean cpfDuplicado = idAtual == null
                ? repository.existsByCpf(cpf)
                : repository.existsByCpfAndIdNot(cpf, idAtual);
        boolean emailDuplicado = idAtual == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, idAtual);
        if (cpfDuplicado) throw new RecursoDuplicadoException("CPF já cadastrado");
        if (emailDuplicado) throw new RecursoDuplicadoException("E-mail já cadastrado");
    }

    private Pessoa buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().forEach(o -> {
            if (!permitidos.contains(o.getProperty()))
                throw new IllegalArgumentException("Campo de ordenação inválido: " + o.getProperty());
        });
    }
}
