package br.com.zupacademy.israel.listarTodasChavesPix

import br.com.zupacademy.israel.KeymanagerListarTodasGrpcServiceGrpc
import br.com.zupacademy.israel.ListarTodasChavesPixRequest
import br.com.zupacademy.israel.ListarTodasChavesPixResponse
import br.com.zupacademy.israel.compartilhado.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
class ListarTodasChavesPixEndPoint(
    @Inject val listarTodasChavesPixService: ListarTodasChavesPixService
) : KeymanagerListarTodasGrpcServiceGrpc.KeymanagerListarTodasGrpcServiceImplBase() {

    override fun listarTodas(
        request: ListarTodasChavesPixRequest,
        responseObserver: StreamObserver<ListarTodasChavesPixResponse>
    ) {
        val clientId = UUID.fromString(request.clientId)
        val todasChavesPixFilter = ListarTodasChavesPixFilter(request.clientId)
        val listaDeChavesPix = listarTodasChavesPixService.listar(todasChavesPixFilter)

        responseObserver.onNext(
            ListarTodasChavesPixResponse.newBuilder()
                .setClientId(clientId.toString())
                .addAllChaves(ListarTodasChavesPixResponseConvert().convert(listaDeChavesPix))
                .build()
        )
        responseObserver.onCompleted()

    }
}