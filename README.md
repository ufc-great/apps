Aplicativos (Descrição)
====

NeTester.apk

Use o NETester para saber rapidamente sobre a qualidade de sua Internet móvel.
Este aplicativo não tem um carácter comercial, apenas acadêmico, pois os dados que serão coletados farão parte de uma pesquisa sobre a qualidade da Internet Móvel no Brasil.

Os dados coletados por este aplicativo:
- ID gerado pelo aplicativo para saber a unicidade da instalação
- Nome do aparelho
- Nome da operadora de celular
- Localidade do usuário (GPS), para guardar a região onde foi feito o teste
- Data do aparelho
- Os resultados do ping do protocolo TCP e UDP
- Quantidade de pacotes perdidos
- Valor do Jitter (Intervalo de chegada dos pacotes)
- Largura de Banda (Download e Upload)
- Tipo de rede usada no momento do teste

Colabore para o sucesso dessa pesquisa.

====

NETester Service

O aplicativo pode ser implantado num servidor ou computador, que utilize a versão do Java 6 da Oracle ou superior. O serviço usa as seguintes portas: UDP 30002, UDP 30010, TCP 30000, TCP 30001, TCP 30011 e TCP 30005.

O JAR pode ser executado através desse comando, mas não esqueça de copiar os arquivos server.log e mpos.data, que devem está no mesmo diretório.

java -jar mpos_service.jar&

====

BenchImage.apk

O aplicativo BenchImage realiza benchmark de CPU através da aplicação de efeitos (filtros) sobre fotos pré-selecionadas no aplicativo. 

Este aplicativo pode executar totalmente no dispositivo móvel ou executar num serviço de processamento de imagem, localizado numa nuvem pública ou num cloudlet (servidor local). No caso do processamento remoto o aplicativo transfere a foto para o servidor e a recebe após o efeito ser aplicado.

O usuario pode exportar o banco de dados (em SQLite) com os valores dos benchmarks realizados!

====

BenchImage Service

O aplicativo pode ser implantado num servidor ou computador, que utilize a versão do Java 6 da Oracle ou superior. O serviço usa a porta TCP 35000.

O JAR pode ser executado através desse comando, mas não esqueça de copiar o arquivo de log (server.log), que deve está no mesmo diretório.

java -jar benchimage_service.jar&

====

Os recursos descritos anteriormente estão disponiveis no diretorio "Release" desse repositório.

