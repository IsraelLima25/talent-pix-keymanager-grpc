# Projeto Micronaut com Kotlin e Protobuf

Este é um projeto de exemplo usando o Micronaut Framework, Kotlin, Protobuf, e outras tecnologias, como Hibernate JPA, GRPC e TestContainers.

## Tecnologias Utilizadas

- **Micronaut**: Framework para aplicações baseadas em microserviços.
- **Kotlin**: Linguagem de programação funcional e moderna para JVM.
- **Protobuf**: Para definição de serviços GRPC.
- **GRPC**: Framework RPC (Remote Procedure Call) de alto desempenho.
- **Hibernate JPA**: Framework ORM para interação com bancos de dados.
- **TestContainers**: Biblioteca para testes com contêineres Docker.

## Requisitos

- Java 11
- Kotlin 1.4.32
- Micronaut 1.5.3
- Gradle

## Como Rodar o Projeto

### Clonando o Repositório

```bash
git clone https://github.com/usuario/projeto.git
cd projeto

### Compilando projeto
./gradlew build

### Executando projeto
./gradlew run

### Executando os testes
./gradlew test
