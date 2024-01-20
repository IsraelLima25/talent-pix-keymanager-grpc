package br.com.zupacademy.israel.registrarNovaChavePix

import br.com.zupacademy.israel.*
import br.com.zupacademy.israel.Titular
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.*
import br.com.zupacademy.israel.compartilhado.apiExterna.itau.DadosDaContaResponse
import br.com.zupacademy.israel.compartilhado.apiExterna.itau.InstituicaoResponse
import br.com.zupacademy.israel.compartilhado.apiExterna.itau.ItauClient
import br.com.zupacademy.israel.compartilhado.apiExterna.itau.TitularResponse
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistrarChavePixEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRegistrarGrpcServiceGrpc.KeymanagerRegistrarGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val ID_CLIENT = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve ser registrada uma nova chave pix`() {

        // cenário
        `when`(itauClient.buscarContaPorTipo(clienteId = ID_CLIENT.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(createDadosDaContaResponse()))

        `when`(
            bcbClient.criarChavePixBCB(
                ChavePixBCBRequest(
                    key = "09851675059",
                    keyType = KeyType.CPF,
                    bankAccount = createBankAccount(),
                    owner = createOwner()
                )
            )
        ).thenReturn(
            createPixKeyResponse()
        )

        // https://www.4devs.com.br/gerador_de_cpf
        // ação
        val response = grpcClient.registrar(
            RegistraChavePixRequest
                .newBuilder()
                .setIdCliente(ID_CLIENT.toString())
                .setTipoChave(TipoChave.CPF)
                .setChave("09851675059")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTitular(
                    Titular.newBuilder().setNome("Sizenando da costa").setDocumento("09851675059")
                        .setTipoTitular(TipoTitular.PESSOA_FISICA)
                )
                .build()
        )

        val novaChavePixCadastrada = repository.findById(UUID.fromString(response.pixId.toString()))

        // validação
        with(response) {
            assertNotNull(pixId)
            assertEquals(pixId, novaChavePixCadastrada.get().id.toString())
        }
    }

    @Test
    fun `nao deve ser registrada chave pix quando a chave ja estiver registrada no banco central`() {
        // cenário
        `when`(itauClient.buscarContaPorTipo(clienteId = ID_CLIENT.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(createDadosDaContaResponse()))

        `when`(
            bcbClient.criarChavePixBCB(
                ChavePixBCBRequest(
                    key = "09851675059",
                    keyType = KeyType.CPF,
                    bankAccount = createBankAccount(),
                    owner = createOwner()
                )
            )
        ).thenReturn(
            HttpResponse.unprocessableEntity()
        )

        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest
                    .newBuilder()
                    .setIdCliente(ID_CLIENT.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setChave("09851675059")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTitular(
                        Titular.newBuilder().setNome("Sizenando da costa").setDocumento("09851675059")
                            .setTipoTitular(TipoTitular.PESSOA_FISICA)
                    )
                    .build()
            )
        }

        with(ex) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave pix ja existe`() {

        // cenário
        val save = repository.save(createChavePix())

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest
                    .newBuilder()
                    .setIdCliente(ID_CLIENT.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setChave("45242085002")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTitular(
                        Titular.newBuilder().setNome("Sizenando da costa").setDocumento("09851675059")
                            .setTipoTitular(TipoTitular.PESSOA_FISICA)
                    )
                    .build()
            )
        }
        // validação
        with(ex) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix já existente!!", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        // cenário
        `when`(itauClient.buscarContaPorTipo(clienteId = ID_CLIENT.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())


        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest
                    .newBuilder()
                    .setIdCliente(ID_CLIENT.toString())
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("sizenando@gmail.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTitular(
                        Titular.newBuilder().setNome("Sizenando da costa").setDocumento("09851675059")
                            .setTipoTitular(TipoTitular.PESSOA_FISICA)
                    )
                    .build()
            )
        }
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, ex.status.code)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(RegistraChavePixRequest.newBuilder().build())
        }
        with(ex) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("request with invalid parameters", status.description)

            val badRequest = StatusProto.fromThrowable(ex)?.detailsList!!.get(0).unpack(BadRequest::class.java)

            val violations = badRequest.fieldViolationsList.map {
                Pair(it.field, it.description)
            }

            assertThat(
                violations, containsInAnyOrder(
                    Pair("idCliente", "must not be blank"),
                    Pair("tipoTitular", "must not be null"),
                    Pair("tipoDeChave", "must not be null"),
                    Pair("tipoConta", "must not be null"),
                    Pair("documento", "must not be blank"),
                    Pair("novaChave", "chave pix inválida"),
                    Pair("nome", "must not be blank"),
                    Pair("chave", "must not be blank")
                )
            )
        }
    }

    private fun createOwner(): Owner {
        return Owner(
            type = TypeOwner.NATURAL_PERSON,
            name = "Sizenando da costa",
            taxIdNumber = "09851675059"
        )
    }

    private fun createDadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            InstituicaoResponse(nome = "Banco itaú", ispb = "41587920"),
            agencia = "0005",
            numero = "15521254",
            TitularResponse(id = "546569aZasq458", nome = "Sizenando M Souza", cpf = "45696354789")
        )
    }

    private fun createBankAccount(): BankAccount {
        return BankAccount(
            participant = "41587920",
            branch = "0005",
            accountNumber = "15521254",
            accountType = AccountType.CACC
        )
    }

    private fun createChavePix(): ChavePix {
        return ChavePix(
            clientId = UUID.randomUUID(),
            tipoDeChave = TipoDeChave.CPF,
            chave = "45242085002",
            TipoConta.CONTA_CORRENTE,
            ContaAssociada(
                instituicao = "Banco Itau",
                nomeTitular = "Sizendo Junior",
                cpfTitular = "45242085002",
                agencia = "12698",
                numeroConta = "15848",
                ispb = "60701190"
            )
        )
    }

    private fun createPixKeyResponse() = HttpResponse.created(
        CreatePixKeyResponse(
            keyType = KeyType.CPF,
            key = "09851675059",
            BankAccount(
                participant = "60701190", branch = "0001",
                accountNumber = "15521254",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.LEGAL_PERSON, name = "Sizenando", taxIdNumber = "09851675059"),
            createdAt = LocalDateTime.now()
        )
    )

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeymanagerRegistrarGrpcServiceGrpc.KeymanagerRegistrarGrpcServiceBlockingStub {
            return KeymanagerRegistrarGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}