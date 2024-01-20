package br.com.zupacademy.israel.removerChavePix

import br.com.zupacademy.israel.KeymanagerRemoverGrpcServiceGrpc
import br.com.zupacademy.israel.RemoverChavePixRequest
import br.com.zupacademy.israel.RemoverChavePixResponseVoid
import br.com.zupacademy.israel.compartilhado.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
class RemoverChavePixEndPoint(val removeChavePixService: RemoverChavePixService) :
    KeymanagerRemoverGrpcServiceGrpc.KeymanagerRemoverGrpcServiceImplBase() {

    override fun remover(
        request: RemoverChavePixRequest,
        responseObserver: StreamObserver<RemoverChavePixResponseVoid>
    ) {
        val removerChavePix = RemoverChavePix(pixId = request.pixId, idCliente = request.idCliente)
        removeChavePixService.remover(removerChavePix)

        responseObserver.onNext(RemoverChavePixResponseVoid.newBuilder().build())
        responseObserver.onCompleted()
    }
}