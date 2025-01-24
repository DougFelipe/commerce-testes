# eCommerce - Finalização de Compra

Este é um projeto de API REST para um sistema de e-commerce, desenvolvido para implementar e testar a funcionalidade de finalização de compras. O sistema utiliza **Spring Boot**, é configurado para rodar com **JUnit 5**, e possui suporte a **JaCoCo** para relatórios de cobertura de testes e **PIT** para testes de mutação.

---

## Requisitos do Projeto

### Funcionalidade Principal
- Implementar a finalização de compras em um e-commerce
- Realizar cálculos de custo total (incluindo regras de frete e descontos)
- Verificar e processar estoque e pagamentos

### Configurações Obrigatórias
1. **JUnit 5**: Para executar os testes automatizados.
2. **JaCoCo**: Para gerar relatórios de cobertura de testes.
3. **PIT**: Para realizar testes de mutação, configurado para usar o grupo "All" de operadores mutantes.
4. **Maven**: Para gerenciar o build e as dependências do projeto.

---

## Como Executar o Projeto

### Pré-requisitos
- **Java 17** ou superior.
- **Maven 3.8+** instalado e configurado no PATH.
- IDE ou terminal configurado para projetos Java/Maven.

---

## Como Executar os Testes

### Testes Automatizados com JUnit 5
1. Para rodar todos os testes automatizados:
```
mvn clean test
```
2. O console exibirá os resultados dos testes.

---

## Como Verificar a Cobertura de Testes (JaCoCo)

1. Execute o comando para gerar o relatório de cobertura:
```
mvn verify
```
2. Localize o relatório HTML gerado no diretório:

target/jacoco-report/index.html

3. Abra o arquivo `index.html` no navegador para visualizar os resultados.

---

## Como Executar os Testes de Mutação (PIT)

1. Certifique-se de que o plugin do **PIT** está configurado corretamente no arquivo `pom.xml`.
2. Execute o comando para rodar os testes de mutação:
```
mvn org.pitest:pitest-maven:mutationCoverage
```
3. Os relatórios gerados estarão disponíveis no diretório:

target/pit-reports/index.html

4. Abra o arquivo `index.html` no navegador para visualizar os resultados detalhados.

---


## Configuração do Maven

### Dependências Principais
- **Spring Boot Starter Web**: Para desenvolvimento da API REST.
- **Spring Boot Starter Data JPA**: Para persistência de dados.
- **H2 Database**: Banco de dados em memória para testes.
- **JUnit 5**: Framework de testes.
- **JaCoCo Maven Plugin**: Para cobertura de testes.
- **PIT Maven Plugin**: Para testes de mutação.

O arquivo `pom.xml` já está configurado com todas as dependências e plugins necessários.

---
