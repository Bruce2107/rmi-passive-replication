# Aplicação tolerante a falha

Projeto para a disciplina de Sistemas Distribuidos - UDESC - Joinville:SC<br>

## Instalação

Clone o repositorio [rmi-passive-replication](https://github.com/Bruce2107/rmi-passive-replication)
    

## Uso

- Iniciar o serviço do MQTT-Broker (docker-compose)
- Gere o arquivo jar da aplicação utilizando a classe Main como principal
- Para gerar o arquivo jar pelo Intellij acesse no menu superior o item Build -> Build Artifactory -> Build
- Execute a arquivo jar pelo terminal usando o comando `java -jar ./mqtt-project.jar <tipo>`
- Valores possiveis para o parametro `tipo`: rmi, server, client
- Obrigatoriamente deve ser executado primeiramente utilizando o tipo rmi

## Exemplo de execução

### Exemplo 1
1. `java -jar ./mqtt-project.jar rmi`
2. `java -jar ./mqtt-project.jar server`
3. `java -jar ./mqtt-project.jar client`
4. `java -jar ./mqtt-project.jar server`
5. `java -jar ./mqtt-project.jar server`

### Exemplo 2
1. `java -jar ./mqtt-project.jar rmi`
2. `java -jar ./mqtt-project.jar server`
3. `java -jar ./mqtt-project.jar server`
4. `java -jar ./mqtt-project.jar server`
5. `java -jar ./mqtt-project.jar client`

### Exemplo 3
1. `java -jar ./mqtt-project.jar rmi`
2. `java -jar ./mqtt-project.jar client`
3. `java -jar ./mqtt-project.jar server`
4. `java -jar ./mqtt-project.jar server`
5. `java -jar ./mqtt-project.jar server`

## Testes

### Teste 1
1. Iniciar o RMI (passo 1 dos exemplos)
2. Inciar um servidor ou o cliente
3. Iniciar os servidores de replica
4. Derrubar o servidor mestre
5. O cliente aguarda 5 segundo após receber uma falha e manda mensagens novamente
6. O servidor de replica 1 deve assumir

### Teste 2
1. Iniciar o RMI (passo 1 dos exemplos)
2. Inciar um servidor ou o cliente
3. Iniciar os servidores de replica
4. Derrubar o servidor de replica 1
5. Derrubar o servidor mestre
5. O cliente aguarda 5 segundo após receber uma falha e manda mensagens novamente
6. O servidor de replica 2 deve assumir

## License

[MIT](https://choosealicense.com/licenses/mit/)
