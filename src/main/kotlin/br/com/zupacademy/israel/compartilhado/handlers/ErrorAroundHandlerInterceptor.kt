package br.com.zupacademy.israel.compartilhado.handlers

import br.com.zupacademy.israel.exception.ChavePixExistenteException
import br.com.zupacademy.israel.exception.ChavePixInexistenteException
import br.com.zupacademy.israel.exception.ClienteNaoEncontradoApiItauException
import br.com.zupacademy.israel.exception.ClienteNaoEncontradoException
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {

            //TODO criar uma execeção customizada para evitar o aninhamento de exceptions
            val statusError = when (e) {
                is ConstraintViolationException -> handleConstraintValidationException(e)
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withCause(e).withDescription(e.message)
                    .asRuntimeException()
                is ClienteNaoEncontradoApiItauException -> Status.NOT_FOUND.withCause(e)
                    .withDescription(e.message).asRuntimeException()
                is ChavePixInexistenteException -> Status.NOT_FOUND.withCause(e).withDescription(e.message)
                    .asRuntimeException()
                is IllegalArgumentException -> Status.FAILED_PRECONDITION.withCause(e).withDescription(e.message)
                    .asRuntimeException()
                is ClienteNaoEncontradoException -> Status.NOT_FOUND.withCause(e).withDescription(e.message)
                    .asRuntimeException()
                else -> Status.UNKNOWN.withDescription("unexpected error happened").asRuntimeException()
            }
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null;
        }
    }

    private fun handleConstraintValidationException(e: ConstraintViolationException): StatusRuntimeException? {
        val badRequest = BadRequest.newBuilder().addAllFieldViolations(e.constraintViolations.map {
            BadRequest.FieldViolation.newBuilder()
                .setField(it.propertyPath.last().name)
                .setDescription(it.message)
                .build()
        }).build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("request with invalid parameters")
            .addDetails(com.google.protobuf.Any.pack(badRequest))
            .build()

        return StatusProto.toStatusRuntimeException(statusProto)
    }
}