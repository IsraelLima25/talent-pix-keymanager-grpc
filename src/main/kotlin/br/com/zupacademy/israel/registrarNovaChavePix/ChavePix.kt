package br.com.zupacademy.israel.registrarNovaChavePix


import br.com.zupacademy.israel.TipoConta
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    @field:Type(type = "org.hibernate.type.UUIDCharType")
    val clientId: UUID,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoDeChave: TipoDeChave,
    @field:NotBlank
    @Column(nullable = false)
    var chave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:Valid
    @field:NotNull
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    @field:Type(type = "org.hibernate.type.UUIDCharType")
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun atualizar(key: String) {
        chave = key
    }

    fun pertenceAo(clienteId: UUID) = this.clientId.equals(clienteId)
}
