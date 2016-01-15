Autenticação por Oauth
======================================================================
A configuração do processo de autenticação por Oauth deve ser feita da mesma forma dos outros métodos de autenticação presentes no Dspace que são descritos na documentação do projeto.

Este arquivo de configuração fica em:

```
./dspace/config/modules/authentication-oauth.cfg
```

Atualizando o Dspace
----------------------------------------------------------------------
Se for necessário fazer alguma alteração nas configurações do dspace, ou nos códigos java do Oauth será necessário efetuar o seguinte comando:

```
cd [dspace-source]
mvn package

cd dspace/target/dspace-installer/
ant update
```

Se o tomcat está configurado para utilizar o webapps do dspace, ou se foi realizada a configuração de mapeamento do tomcat para os endereços do dspace, nada mais precisa ser feito. Mas se na instalação foi apenas copiado o conteúdo do webapps do dspace para o tomcat, isso deve ser realizado novamente, pois com o update os arquivos foram recriados.