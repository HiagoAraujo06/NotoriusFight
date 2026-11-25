package com.notoriusfight.api;

import com.jayway.jsonpath.JsonPath;
import com.notoriusfight.domain.ContaAcesso;
import com.notoriusfight.domain.PerfilAcesso;
import com.notoriusfight.domain.Pessoa;
import com.notoriusfight.domain.Status;
import com.notoriusfight.repository.ContaAcessoRepository;
import com.notoriusfight.repository.EventoSegurancaRepository;
import com.notoriusfight.repository.PessoaRepository;
import com.notoriusfight.repository.ProtecaoAutenticacaoRepository;
import com.notoriusfight.repository.SessaoRefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Usa um servidor real: o MockMvc não executa o despacho de erro do container,
 * que é justamente onde os status HTTP podem ser mascarados pela segurança.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegressaoHttpTest {

    private static final String SENHA = "senha-regressao-2026";

    @Value("${local.server.port}")
    private int porta;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ContaAcessoRepository contaRepository;

    @Autowired
    private SessaoRefreshTokenRepository sessaoRepository;

    @Autowired
    private EventoSegurancaRepository eventoRepository;

    @Autowired
    private ProtecaoAutenticacaoRepository protecaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final List<ContaAcesso> contas = new ArrayList<>();

    @AfterEach
    void limparCenario() {
        eventoRepository.deleteAll();
        protecaoRepository.deleteAll();
        sessaoRepository.deleteAll();
        contas.forEach(conta -> {
            contaRepository.deleteById(conta.getId());
            pessoaRepository.deleteById(conta.getPessoa().getId());
        });
    }

    @Test
    void errosDeProtocoloDevemManterOStatusVerdadeiro() throws Exception {
        criarConta("regressao.erros", PerfilAcesso.OPERADOR);
        String token = login("regressao.erros", SENHA).accessToken();

        HttpResponse<String> parametroInvalido = enviar("GET", "/api/artigos/abc", token, null);
        assertEquals(400, parametroInvalido.statusCode());
        assertEquals("Valor inválido para o parâmetro: id",
                JsonPath.read(parametroInvalido.body(), "$.message"));
        assertEquals(400,
                enviar("GET", "/api/artigos?modalidade=XADREZ", token, null).statusCode());
        assertEquals(405, enviar("PATCH", "/api/artigos/1", token, "{}").statusCode());
        assertEquals(404, enviar("GET", "/api/rota-inexistente", token, null).statusCode());
        assertEquals(401, enviar("GET", "/api/artigos", null, null).statusCode());

        HttpResponse<String> tipoInvalido = cliente.send(
                HttpRequest.newBuilder(uri("/api/autenticacao/login"))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString("login"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(415, tipoInvalido.statusCode());
    }

    @Test
    void operadorNaoDeveInativarAPessoaDeUmAdministrador() throws Exception {
        ContaAcesso administrador = criarConta("regressao.admin", PerfilAcesso.ADMINISTRADOR);
        criarConta("regressao.operador", PerfilAcesso.OPERADOR);
        String token = login("regressao.operador", SENHA).accessToken();

        HttpResponse<String> resposta = inativar(administrador, token);

        assertEquals(403, resposta.statusCode());
        assertEquals(Status.ATIVO, statusDaPessoa(administrador));
    }

    @Test
    void sistemaDeveManterAoMenosUmAdministradorComPessoaAtiva() throws Exception {
        ContaAcesso primeiro = criarConta("regressao.admin1", PerfilAcesso.ADMINISTRADOR);
        ContaAcesso segundo = criarConta("regressao.admin2", PerfilAcesso.ADMINISTRADOR);
        String token = login("regressao.admin1", SENHA).accessToken();

        assertEquals(200, inativar(segundo, token).statusCode());
        HttpResponse<String> ultimo = inativar(primeiro, token);

        assertEquals(400, ultimo.statusCode());
        assertEquals("O sistema deve manter ao menos um administrador ativo",
                JsonPath.read(ultimo.body(), "$.message"));
        assertEquals(Status.ATIVO, statusDaPessoa(primeiro));
    }

    @Test
    void trocaDeSenhaDeveRevogarAsSessoesAbertas() throws Exception {
        criarConta("regressao.senha", PerfilAcesso.OPERADOR);
        Tokens tokens = login("regressao.senha", SENHA);
        String novaSenha = "outra-senha-regressao-2026";

        assertEquals(204, enviar("PUT", "/api/contas-acesso/me/senha", tokens.accessToken(), """
                {"senhaAtual":"%s","novaSenha":"%s"}
                """.formatted(SENHA, novaSenha)).statusCode());

        assertEquals(401, renovar(tokens).statusCode());
        assertTrue(login("regressao.senha", novaSenha).accessToken().length() > 20);
    }

    @Test
    void bloqueioDaContaDeveRevogarSessoesMesmoAposReativacao() throws Exception {
        criarConta("regressao.gestor", PerfilAcesso.ADMINISTRADOR);
        ContaAcesso operador = criarConta("regressao.bloqueado", PerfilAcesso.OPERADOR);
        String token = login("regressao.gestor", SENHA).accessToken();
        Tokens sessao = login("regressao.bloqueado", SENHA);
        String rota = "/api/contas-acesso/" + operador.getId() + "/status";

        assertEquals(200, enviar("PUT", rota, token, "{\"status\":\"BLOQUEADA\"}").statusCode());
        assertEquals(200, enviar("PUT", rota, token, "{\"status\":\"ATIVA\"}").statusCode());

        assertEquals(401, renovar(sessao).statusCode());
    }

    private HttpResponse<String> inativar(ContaAcesso conta, String token) throws Exception {
        return enviar(
                "PUT",
                "/api/pessoas/" + conta.getPessoa().getId() + "/status",
                token,
                "{\"status\":\"INATIVO\"}");
    }

    private HttpResponse<String> renovar(Tokens tokens) throws Exception {
        return enviar("POST", "/api/autenticacao/refresh", null, """
                {"refreshToken":"%s"}
                """.formatted(tokens.refreshToken()));
    }

    private Status statusDaPessoa(ContaAcesso conta) {
        return pessoaRepository.findById(conta.getPessoa().getId()).orElseThrow().getStatus();
    }

    private Tokens login(String login, String senha) throws Exception {
        HttpResponse<String> resposta = enviar("POST", "/api/autenticacao/login", null, """
                {"login":"%s","senha":"%s"}
                """.formatted(login, senha));
        assertEquals(200, resposta.statusCode());
        return new Tokens(
                JsonPath.read(resposta.body(), "$.accessToken"),
                JsonPath.read(resposta.body(), "$.refreshToken"));
    }

    private HttpResponse<String> enviar(String metodo, String rota, String token, String corpo)
            throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(uri(rota))
                .header("Content-Type", "application/json")
                .method(metodo, corpo == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(corpo));
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String rota) {
        return URI.create("http://localhost:" + porta + rota);
    }

    private ContaAcesso criarConta(String login, PerfilAcesso perfil) {
        long semente = Math.abs(System.nanoTime());
        Pessoa pessoa = pessoaRepository.saveAndFlush(new Pessoa(
                "Pessoa Regressão",
                "regressao." + semente + "@example.com",
                gerarCpf(semente),
                LocalDate.of(2026, 11, 24)));
        ContaAcesso conta = new ContaAcesso(pessoa, login, passwordEncoder.encode(SENHA));
        conta.concederPerfil(perfil);
        conta = contaRepository.saveAndFlush(conta);
        contas.add(conta);
        return conta;
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(semente % 1_000_000_000L);
        if (base.chars().distinct().count() == 1) {
            base = "123456789";
        }
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String numeros, int pesoInicial) {
        int soma = 0;
        for (int indice = 0; indice < numeros.length(); indice++) {
            soma += Character.getNumericValue(numeros.charAt(indice)) * (pesoInicial - indice);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private record Tokens(String accessToken, String refreshToken) {
    }
}
