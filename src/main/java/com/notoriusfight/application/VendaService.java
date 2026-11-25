package com.notoriusfight.application;

import com.notoriusfight.domain.Cliente;
import com.notoriusfight.domain.Colaborador;
import com.notoriusfight.domain.FuncaoColaborador;
import com.notoriusfight.domain.ItemVenda;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.domain.Status;
import com.notoriusfight.domain.StatusVenda;
import com.notoriusfight.domain.Venda;
import com.notoriusfight.repository.ClienteRepository;
import com.notoriusfight.repository.ColaboradorRepository;
import com.notoriusfight.repository.ArtigoRepository;
import com.notoriusfight.repository.VendaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class VendaService {
    private final VendaRepository repository;
    private final ClienteRepository clienteRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final ArtigoRepository artigoRepository;

    public VendaService(VendaRepository repository, ClienteRepository clienteRepository,
                        ColaboradorRepository colaboradorRepository, ArtigoRepository artigoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.artigoRepository = artigoRepository;
    }

    @Transactional
    public Venda cadastrar(Long clienteId, Long vendedorId) {
        Cliente cliente = buscarCliente(clienteId);
        Colaborador vendedor = buscarVendedor(vendedorId);
        validarParticipantes(cliente, vendedor);
        return repository.save(new Venda(cliente, vendedor, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public Venda buscarPorId(Long id) { return buscarEntidade(id); }

    @Transactional
    public Venda adicionarItem(Long vendaId, Long artigoId, BigDecimal quantidade) {
        Venda venda = buscarEntidade(vendaId);
        Artigo artigo = artigoRepository.buscarPorIdComRelacionamentos(artigoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Artigo não encontrado"));
        if (artigo.getStatus() != Status.ATIVO) throw new IllegalArgumentException("Artigo deve estar ativo");
        venda.adicionarItem(artigo, quantidade);
        return venda;
    }

    @Transactional
    public Venda removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarEntidade(vendaId);
        venda.removerItem(itemId);
        return venda;
    }

    @Transactional
    public Venda finalizar(Long id) {
        Venda venda = buscarEntidade(id);
        validarParticipantes(venda.getCliente(), venda.getVendedor());
        for (ItemVenda item : venda.getItens()) {
            if (item.getArtigo().getStatus() != Status.ATIVO)
                throw new IllegalArgumentException("Todos os artigos da venda devem estar ativos");
            item.getArtigo().retirarEstoque(item.getQuantidade());
        }
        venda.finalizar();
        return venda;
    }

    @Transactional
    public Venda cancelar(Long id) {
        Venda venda = buscarEntidade(id);
        venda.cancelar();
        return venda;
    }

    @Transactional(readOnly = true)
    public Page<Venda> pesquisar(StatusVenda status, Long clienteId, Long vendedorId,
                                  LocalDate dataInicial, LocalDate dataFinal, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "dataVenda", "status"));
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal))
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        Specification<Venda> filtros = (root, query, cb) -> cb.conjunction();
        if (status != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (clienteId != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        if (vendedorId != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("vendedor").get("id"), vendedorId));
        if (dataInicial != null) filtros = filtros.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataVenda"), dataInicial.atStartOfDay()));
        if (dataFinal != null) filtros = filtros.and((root, query, cb) -> cb.lessThan(root.get("dataVenda"), dataFinal.plusDays(1).atStartOfDay()));
        return repository.findAll(filtros, pageable);
    }

    private void validarParticipantes(Cliente cliente, Colaborador vendedor) {
        if (cliente.getPessoa().getStatus() != Status.ATIVO) throw new IllegalArgumentException("Cliente deve estar ativo");
        if (vendedor.getPessoa().getStatus() != Status.ATIVO) throw new IllegalArgumentException("Vendedor deve estar ativo");
        if (vendedor.getFuncao() != FuncaoColaborador.VENDEDOR && vendedor.getFuncao() != FuncaoColaborador.GERENTE)
            throw new IllegalArgumentException("Colaborador não possui função habilitada para vender");
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private Colaborador buscarVendedor(Long id) {
        return colaboradorRepository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    private Venda buscarEntidade(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().forEach(o -> {
            if (!permitidos.contains(o.getProperty())) throw new IllegalArgumentException("Campo de ordenação inválido: " + o.getProperty());
        });
    }
}
