package br.com.zupacademy.israel.registrarNovaChavePix

import br.com.zupacademy.israel.KeymanagerRegistrarGrpcServiceGrpc
import br.com.zupacademy.israel.RegistraChavePixRequest
import br.com.zupacademy.israel.RegistraChavePixResponse
import br.com.zupacademy.israel.compartilhado.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
class RegistrarChavePixEndPoint(@Inject val novaChavePixService: NovaChavePixService) :
    KeymanagerRegistrarGrpcServiceGrpc.KeymanagerRegistrarGrpcServiceImplBase() {

    val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun registrar(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChave = request.toModel()
        LOGGER.info("Registrando nova chave pix")
        val novaChavePix = novaChavePixService.registrarNovaChavePix(novaChave)

        responseObserver.onNext(
            RegistraChavePixResponse
                .newBuilder()
                .setPixId(novaChavePix.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}