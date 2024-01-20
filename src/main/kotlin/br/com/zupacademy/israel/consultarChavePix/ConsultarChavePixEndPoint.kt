package br.com.zupacademy.israel.consultarChavePix

import br.com.zupacademy.israel.ConsultarChavePixRequest
import br.com.zupacademy.israel.ConsultarChavePixResponse
import br.com.zupacademy.israel.KeymanagerConsultarGrpcServiceGrpc
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.BcbClient
import br.com.zupacademy.israel.compartilhado.handlers.ErrorAroundHandler
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAroundHandler
@Singleton
class ConsultarChavePixEndPoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient
) : KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceImplBase() {

    override fun consultar(
        request: ConsultarChavePixRequest,
        responseObserver: StreamObserver<ConsultarChavePixResponse>
    ) {
        val filtro = request.toModel()
        val chaveInfo = filtro.filtrar(repository, bcbClient)

        responseObserver.onNext(ConsultarChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}