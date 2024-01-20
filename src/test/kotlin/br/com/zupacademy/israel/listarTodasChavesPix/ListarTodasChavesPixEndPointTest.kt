package br.com.zupacademy.israel.listarTodasChavesPix

import br.com.zupacademy.israel.*
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListarTodasChavesPixEndPointTest(
    val grpcClient: KeymanagerListarTodasGrpcServiceGrpc.KeymanagerListarTodasGrpcServiceBlockingStub,
    val repository: ChavePixRepository
) {

    lateinit var CLIENT_ID: UUID

    @BeforeEach
    internal fun setUp() {

        CLIENT_ID = UUID.randomUUID()

        repository.save(
            ChavePix(
                clientId = CLIENT_ID,
                tipoDeChave = TipoDeChave.CPF,
                chave = "45242085002",
                TipoConta.CONTA_CORRENTE,
                ContaAssociada(
                    instituicao = "Banco Itau",
                    nomeTitular = "Sizenando Junior",
                    cpfTitular = "45242085002",
                    agencia = "12698",
                    numeroConta = "15848",
                    ispb = "60701190"
                )
            )
        )
        repository.save(
            ChavePix(
                clientId = CLIENT_ID,
                tipoDeChave = TipoDeChave.EMAIL,
                chave = "size@gmail.com",
                TipoConta.CONTA_CORRENTE,
                ContaAssociada(
                    instituicao = "Banco Itau",
                    nomeTitular = "Sizenando Junior",
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
    fun `deve listar todas as chaves pix do cliente`() {

        // ação
        val response = grpcClient.listarTodas(
            ListarTodasChavesPixRequest
                .newBuilder()
                .setClientId(
                    CLIENT_ID
                        .toString()
                )
                .build()
        )

        // validação
        with(response) {
            assertTrue(response.chavesList.size == 2)
            assertEquals(TipoChave.EMAIL, response.chavesList.get(1).tipoDeChave)
            assertEquals(TipoChave.CPF, response.chavesList.get(0).tipoDeChave)
            assertEquals("45242085002", response.chavesList.get(0).valorChave)
            assertEquals("size@gmail.com", response.chavesList.get(1).valorChave)
        }
    }

    @Test
    fun `deve retornar uma colecao vazia quando cliente nao possui chave cadastrada`() {

        //cenário
        val clientIdInexistente = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.listarTodas(
            ListarTodasChavesPixRequest
                .newBuilder()
                .setClientId(
                    clientIdInexistente
                )
                .build()
        )
        // validação
        with(response) {
            assertTrue(response.chavesList.isEmpty())
        }
    }

    @Test
    fun `deve lancar excessao quando os dados da consulta forem invalidos`() {

        // cenário
        val clientIdInvalida = ""
        // ação
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.listarTodas(
                ListarTodasChavesPixRequest
                    .newBuilder()
                    .setClientId(clientIdInvalida)
                    .build()
            )
        }
        // validação
        with(ex) {
            assertEquals(Status.FAILED_PRECONDITION.code, ex.status.code)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeymanagerListarTodasGrpcServiceGrpc.KeymanagerListarTodasGrpcServiceBlockingStub {
            return KeymanagerListarTodasGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}