package br.com.zupacademy.israel.consultarChavePix

import br.com.zupacademy.israel.ConsultarChavePixRequest.FiltroPorPixId
import br.com.zupacademy.israel.ConsultarChavePixRequest.newBuilder
import br.com.zupacademy.israel.KeymanagerConsultarGrpcServiceGrpc
import br.com.zupacademy.israel.TipoConta
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.*
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePix
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import br.com.zupacademy.israel.registrarNovaChavePix.ContaAssociada
import br.com.zupacademy.israel.registrarNovaChavePix.TipoDeChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class ConsultarChavePixEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
        val chavePix = ChavePix(
            clientId = UUID.randomUUID(),
            tipoDeChave = TipoDeChave.CPF,
            chave = "45242085002",
            TipoConta.CONTA_CORRENTE,
            ContaAssociada(
                instituicao = "Banco Itau",
                nomeTitular = "Sizendo Junior",
                cpfTitular = "22139509030",
                agencia = "12698",
                numeroConta = "15848",
                ispb = "60701190"
            )
        )
        repository.save(chavePix)
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }


    @Test
    fun `deve consultar chave pix por id do cliente e id da chave`() {

        // cenário
        val chaveExistente = repository.findByChave("45242085002").get()

        // ação
        val response = grpcClient.consultar(
            newBuilder()
                .setPixId(
                    FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClientId(chaveExistente.clientId.toString())
                        .build()
                ).build()
        )

        // validação

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clientId.toString(), this.clienteId)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {

        // cenário
        val pixIdInexistente = UUID.randomUUID()
        val idClientInexistente = UUID.randomUUID()

        // ação
        val ex = assertThrows<StatusRuntimeException> {

            grpcClient.consultar(
                newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId(pixIdInexistente.toString())
                            .setClientId(idClientInexistente.toString())
                            .build()
                    ).build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, ex.status.code)
            assertEquals("chave pix não encontrada", ex.status.description)
        }

    }

    @Test
    fun `nao deve consultar chave pix por id do cliente e id da chave quando os dados nao forem preenchidos`() {

        // cenário
        val pixIdInexistente = ""
        val idClientInexistente = ""

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId(pixIdInexistente)
                            .setClientId(idClientInexistente)
                            .build()
                    ).build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.FAILED_PRECONDITION.code, ex.status.code)
            assertEquals("Os dados não devem estar em branco", ex.status.description)
        }
    }

    @Test
    fun `nao deve consultar chave pix quando cliente nao for o dono da chave`() {
        // cenário
        val pixIdExistente = repository.findByChave("45242085002").get().id.toString()
        val idClientInexistente = UUID.randomUUID().toString()

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId(pixIdExistente)
                            .setClientId(idClientInexistente)
                            .build()
                    ).build()
            )
        }
        // validação
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, ex.status.code)
            assertEquals("chave pix não encontrada", ex.status.description)
        }
    }

    @Test
    fun `deve consultar chave pix no banco central e encontrar quando a chave nao for encontrada localmente`() {

        // cenário
        repository.deleteAll()
        `when`(bcbClient.buscarPorChave("45242085002"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // ação
        val response = grpcClient.consultar(
            newBuilder()
                .setChave("45242085002").build()
        )

        // validação

        with(response) {
            assertEquals("45242085002", this.chave.chave)
            assertEquals("Sizendo Junior", this.chave.conta.nomeDoTitular)
        }
    }

    @Test
    fun `deve consultar chave por valor da chave quando registro existir localmente`() {

        // cenário
        val chaveExistente = repository.findByChave("45242085002").get()
        // ação
        val response = grpcClient.consultar(
            newBuilder()
                .setChave("45242085002")
                .build()
        )
        // validação
        with(response) {
            assertEquals("45242085002", response.chave.chave)
            assertEquals("22139509030", response.chave.conta.cpfDoTitular)
            assertEquals("Sizendo Junior", response.chave.conta.nomeDoTitular)
        }
    }

    @Test
    fun `nao deve consultar chave por valor da chave quando registro nao existir localmente nem no banco central`() {

        // cenário
        val chaveInexistente = "45242085003"
        `when`(bcbClient.buscarPorChave(chaveInexistente))
            .thenReturn(HttpResponse.notFound())

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                newBuilder()
                    .setChave(chaveInexistente)
                    .build()
            )
        }
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, ex.status.code)
        }
    }

    @Test
    fun `quando consultar chave pix por chave o dado deve ser valido`() {

        // cenário
        val chaveEmBranco = ""

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                newBuilder().setChave(chaveEmBranco).build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.FAILED_PRECONDITION.code, ex.status.code)
            assertEquals("Os dados não devem estar em branco e não devem ser maior que 77 caracteres", ex.status.description)
        }
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyType.CPF,
            key = "45242085002",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "60701190",
            branch = "12698",
            accountNumber = "15848",
            accountType = AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = TypeOwner.NATURAL_PERSON,
            name = "Sizendo Junior",
            taxIdNumber = "45242085002"
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceBlockingStub? {
            return KeymanagerConsultarGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}