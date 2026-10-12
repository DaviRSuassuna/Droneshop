package br.com.droneshop.endereco;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ViaCEP falso, que só existe nos testes (src/test).
 *
 * No perfil %test o REST Client aponta para cá
 * (quarkus.rest-client.viacep.url no application.properties), então nenhum
 * teste depende da internet nem do ViaCEP estar no ar. Cada CEP abaixo
 * reproduz uma resposta real do ViaCEP.
 */
@Path("/viacep-falso/ws")
@Produces(MediaType.APPLICATION_JSON)
public class ViaCepFalso {

    /** CEP encontrado, com código IBGE de Palmas. */
    public static final String CEP_PALMAS = "77001002";
    /** CEP sem código IBGE e com logradouro vazio (CEP geral de cidade): cai no plano B nome + UF. */
    public static final String CEP_SEM_IBGE = "77000000";
    /** CEP de uma cidade que não está no localidades.sql: município volta null. */
    public static final String CEP_CIDADE_DESCONHECIDA = "77999000";
    /** Formato válido, mas o CEP não existe: o ViaCEP responde 200 com {"erro": "true"}. */
    public static final String CEP_INEXISTENTE = "99999999";
    /** ViaCEP fora do ar (erro HTTP): a API responde 503. */
    public static final String CEP_VIACEP_FORA = "55555555";

    @GET
    @Path("/{cep}/json/")
    public Response buscar(@PathParam("cep") String cep) {
        return switch (cep) {
            case CEP_PALMAS -> Response.ok(Map.of(
                    "cep", "77001-002",
                    "logradouro", "Quadra 104 Norte Avenida LO 2",
                    "complemento", "",
                    "bairro", "Plano Diretor Norte",
                    "localidade", "Palmas",
                    "uf", "TO",
                    "ibge", "1721000")).build();
            case CEP_SEM_IBGE -> Response.ok(Map.of(
                    "cep", "77000-000",
                    "logradouro", "",
                    "complemento", "",
                    "bairro", "",
                    "localidade", "Palmas",
                    "uf", "TO",
                    "ibge", "")).build();
            case CEP_CIDADE_DESCONHECIDA -> Response.ok(Map.of(
                    "cep", "77999-000",
                    "logradouro", "Rua Teste",
                    "bairro", "Centro",
                    "localidade", "Cidade Que Nao Existe",
                    "uf", "TO",
                    "ibge", "")).build();
            case CEP_VIACEP_FORA -> Response.serverError().build();
            default -> Response.ok(Map.of("erro", "true")).build();
        };
    }
}
