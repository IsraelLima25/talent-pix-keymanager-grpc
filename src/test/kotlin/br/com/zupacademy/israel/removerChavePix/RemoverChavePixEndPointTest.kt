package br.com.zupacademy.israel.removerChavePix

import br.com.zupacademy.israel.KeymanagerRemoverGrpcServiceGrpc
import br.com.zupacademy.israel.RemoverChavePixRequest
import br.com.zupacademy.israel.TipoConta
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.BcbClient
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.DeletePixKeyRequest
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.DeletePixKeyResponse
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePix
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import br.com.zupacademy.israel.registrarNovaChavePix.ContaAssociada
import br.com.zupacademy.israel.registrarNovaChavePix.TipoDeChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChavePixEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRemoverGrpcServiceGrpc.KeymanagerRemoverGrpcServiceBlockingStub

) {

    lateinit var CHAVE_EXISTENTE: ChavePix

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    internal fun setUp() {
        CHAVE_EXISTENTE = repository.save(
            ChavePix(
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
        )
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `chave pix deve ser removida`() {

        //cenário
        `when`(
            bcbClient.removerChavePixBcb(
                key = "45242085002",
                deletePixKeyRequest = DeletePixKeyRequest(key = "45242085002", participant = "60701190")
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyResponse(
                    key = "09851675059",
                    participant = "60701190",
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        // ação
        val response = grpcClient.remover(
            RemoverChavePixRequest
                .newBuilder()
                .setIdCliente(CHAVE_EXISTENTE.clientId.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build()
        )


        val existeChavePix = repository
            .findById(UUID.fromString(CHAVE_EXISTENTE.id.toString()))
            .isPresent

        // validação
        assertFalse(existeChavePix)
    }

    @Test
    fun `quando chave pix nao for encontrada no banco central um 404 deve ser lancado e uma mensagem amigavel exposta`() {

        //cenário
        `when`(
            bcbClient.removerChavePixBcb(
                key = "45242085002",
                deletePixKeyRequest = DeletePixKeyRequest(key = "45242085002", participant = "60701190")
            )
        ).thenReturn(
            HttpResponse.notFound()
        )

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setIdCliente(CHAVE_EXISTENTE.clientId.toString())
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .build()
            )
        }

        with(ex){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada", status.description)
        }
    }

    @Test
    fun `quando chave pix e id do cliente nao existir a chave pix nao deve ser removida`() {

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setIdCliente(UUID.randomUUID().toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(status.description, "Chave pix não encontrada")
        }
    }

    @Test
    fun `quando chave pix nao existir e id do cliente pertencer ao usuario a chave pix nao deve ser removida`() {

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setIdCliente(CHAVE_EXISTENTE.clientId.toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(status.description, "Chave pix não encontrada")
        }
    }

    @Test
    fun `quando chave pix existir e id do cliente pertencer a outro cliente a chave pix nao deve ser removida`() {

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setIdCliente(UUID.randomUUID().toString())
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `chave pix nao deve ser removida quando valores forem invalidos`() {

        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .build()
            )
        }

        // validação
        with(ex) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeymanagerRemoverGrpcServiceGrpc.KeymanagerRemoverGrpcServiceBlockingStub {
            return KeymanagerRemoverGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }
}