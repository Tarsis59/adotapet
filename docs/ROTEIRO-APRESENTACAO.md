# 🎤 Roteiro da apresentação: AdotaPet

> **Duração total:** cerca de 12 minutos (8 de slides e demonstração intercalados, mais perguntas).
> **Slides:** `docs/apresentacao/AdotaPet-Apresentacao.pptx` (as falas também estão nas anotações do apresentador) ou `AdotaPet-Apresentacao.pdf`.
> **Quem conduz o computador:** Társis (abre os slides e faz a demonstração; os colegas falam e indicam o que mostrar).

| Integrante | Slides | Tempo aproximado |
|---|---|---|
| Társis Carvalho Barreto | 1, 2, 8 e 16 | 2 min 30 s |
| Ylo Silva de Sá Bittencourt | 3, 4, 7 e 14 | 2 min 30 s |
| Guilherme Crocamo Freire | 5, 6 e 13 | 2 min 30 s |
| Samuel Ferreira Santos | 9, 11 (detalhes) e 12 | 2 min |
| João Pedro França Chaves Oliveira | 10 e 11 (edição) | 2 min |

---

## 1. Preparação (fazer **30 minutos antes**, no computador que vai apresentar)

### 1.1 Programas necessários
- **Java 21**: confira com `java -version`
- **Node.js 22.22 ou superior**: confira com `node -v`
- **Docker Desktop** aberto e rodando (para o PostgreSQL). Sem Docker, veja o **Plano B** abaixo.
- **Git**, para clonar o repositório

### 1.2 Baixar o projeto (só na primeira vez)
```bash
git clone https://github.com/Tarsis59/adotapet.git
cd adotapet
```

### 1.3 Subir o sistema: 3 terminais
**Terminal 1, banco de dados:**
```bash
docker compose up -d
```

**Terminal 2, backend (API na porta 8080):**
```bash
cd backend
mvnw.cmd spring-boot:run        # no Windows
./mvnw spring-boot:run          # no Linux/Mac
```
Espere aparecer `Started AdotapetApiApplication`. Na primeira vez o Maven baixa as dependências, o que leva de 2 a 5 minutos.

**Terminal 3, frontend (porta 4200):**
```bash
cd frontend
npm install      # só na primeira vez
npm start
```
Espere aparecer `Local: http://localhost:4200/`.

### 1.4 Deixar abas prontas no navegador
1. **Aba anônima** em `http://localhost:4200` (para começar sem sessão).
2. Aba com o **Swagger**: `http://localhost:8080/swagger-ui.html`
3. Os slides abertos em tela cheia (PowerPoint → **Apresentação de Slides** → **Do Começo**, ou F5).
4. DevTools (F12) na aba do sistema, na guia **Network**, filtrando por `api`.

### 1.5 Checklist rápido antes de começar
- [ ] `http://localhost:4200` abre a tela de login
- [ ] O login com `admin@adotapet.com` / `Admin@123` funciona
- [ ] A listagem mostra os animais de exemplo
- [ ] Clicar em **Sair** volta para o login (deixe deslogado para a demo)
- [ ] Zoom do navegador em 110% a 125%, para a turma enxergar bem

### Plano B (sem Docker ou se a porta do banco estiver ocupada)
```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```
Usa o banco H2 salvo em arquivo (`backend/data/`). Funciona igual e também mantém os dados após reiniciar.

> **Se a porta 8080 estiver ocupada:** feche o programa que a usa ou rode o backend com `--server.port=8081` e ajuste `frontend/src/environments/environment.development.ts` para `http://localhost:8081/api`.

---

## 2. Roteiro com as falas

### Slide 1 · Capa (Társis)
> "Boa noite, professor e turma. Nós somos o grupo do **AdotaPet**: eu, Társis, o Ylo, o Guilherme, o Samuel e o João Pedro. O AdotaPet é um MVP web full stack para ONGs e abrigos gerenciarem os animais disponíveis para adoção. O frontend é em Angular, o backend em Spring Boot, a autenticação usa JWT e os dados ficam em um banco PostgreSQL."

### Slide 2 · O problema (Társis)
> "O problema que escolhemos é bem concreto: hoje muitas ONGs controlam os animais em planilhas, cadernos e grupos de WhatsApp. Com isso a informação fica espalhada e se perde quando os voluntários mudam, animais que já foram adotados continuam sendo divulgados, e não existe um histórico confiável de cada animal. Agora o Ylo apresenta o público-alvo."

### Slide 3 · Público-alvo e proposta (Ylo)
> "O sistema foi pensado para voluntários e administradores de ONGs, abrigos e lares temporários, e também para protetores independentes. A proposta é simples: um cadastro único e seguro de cada animal, com o status de adoção sempre atualizado (disponível, em processo ou adotado), busca e filtros para achar rápido quem está disponível, e acesso restrito a quem tem login."

### Slide 4 · Funcionalidades (Ylo)
> "As funcionalidades são: login validado no backend com JWT, cadastro de conta de voluntário, rotas protegidas e opção de sair. A listagem traz indicadores, busca, filtros e paginação. Temos cadastro e edição com validação, a página de detalhes e a exclusão sempre com confirmação. Todas as operações mostram mensagem de sucesso ou de falha, e a interface é responsiva. O Guilherme explica a arquitetura."

### Slide 5 · Arquitetura (Guilherme)
> "A aplicação tem três camadas. O frontend em Angular 22 usa o Angular Router com lazy loading e consome a API por meio de serviços. Um interceptor coloca o token em todas as requisições e os guards protegem as rotas. O backend em Spring Boot 4 segue a separação em controllers, services e repositories, com DTOs na entrada e na saída e validação com Bean Validation. A persistência é feita pelo Spring Data JPA em um PostgreSQL rodando no Docker, e também deixamos o H2 em arquivo como alternativa."

### Slide 6 · Backend e CRUD (Guilherme)
> "A entidade principal do CRUD é o **Animal**. O cadastro responde 201 Created com o cabeçalho Location. A listagem é paginada. A consulta por id, a atualização e a exclusão respondem 200 e 204, ou 404 quando o registro não existe. Os dados de entrada passam pelo DTO validado, e se algo vier inválido a API responde 400 com a lista de campos e as mensagens em português. Tudo isso é tratado de forma centralizada no GlobalExceptionHandler."
>
> *(Opcional: mostrar rapidamente a aba do Swagger com os endpoints.)*

### Slide 7 · Segurança com JWT (Ylo)
> "A autenticação usa Spring Security e JWT. O usuário faz login, o backend compara a senha com o hash BCrypt salvo no banco (a senha nunca fica em texto puro) e devolve um token assinado que expira em 2 horas. O Angular envia esse token no cabeçalho Authorization Bearer, e um filtro valida a assinatura e a expiração antes de liberar o acesso. A API diferencia os erros: credenciais incorretas, falta de autenticação, token inválido e token expirado. E a senha nunca volta em nenhuma resposta."

### Slide 8 · DEMO: Login (Társis, no computador)
> "Agora vamos à demonstração."

1. Na aba anônima, digite `localhost:4200/animais` → **o sistema redireciona para o login** (rota protegida).
   > "Sem estar logado, não dá para acessar a listagem: o guard manda para o login."
2. Digite `admin@adotapet.com` e uma senha **errada** → aparece **"E-mail ou senha inválidos."**
   > "Essa mensagem vem do backend: o login não é simulado."
3. Digite a senha `Admin@123` → entra na listagem.
4. No DevTools (Network), clique na requisição `login`: mostre o **token** na resposta e que **não existe campo senha**. Clique em `animais` e mostre o cabeçalho **Authorization: Bearer ...**

### Slide 9 · DEMO: Listagem (Samuel)
> "Esta é a listagem. No topo ficam os indicadores: total de animais, disponíveis, em processo e adotados. Todos esses dados vêm da API e do banco."

- Digite **"Luna"** na busca → a lista filtra.
- Limpe e escolha **Espécie: Gato**, depois **Status: Disponível**.
- Clique em **Limpar filtros** e mostre a **paginação** no rodapé.
> "A busca, os filtros e a paginação são feitos no servidor. A partir de cada linha é possível ver, editar ou excluir. O João vai mostrar o cadastro."

### Slide 10 · DEMO: Cadastro (João Pedro)
- Clique em **Novo animal** e em **Cadastrar animal** com o formulário vazio.
> "Se eu tentar salvar vazio, o formulário mostra uma mensagem clara em cada campo obrigatório. As mesmas regras também existem no backend."
- Preencha: Nome **Paçoca**, Espécie **Cachorro**, Raça **Vira-lata caramelo**, Sexo **Macho**, Porte **Médio**, Idade **2**, Status **Disponível**, marque **Vacinado**, escreva uma descrição curta e clique em **Cadastrar animal**.
> "Ao salvar, aparece a mensagem de sucesso e o sistema abre a página de detalhes do animal recém-cadastrado."

### Slide 11 · DEMO: Detalhes (Samuel) e Edição (João Pedro)
**Samuel:**
> "Esta é a página de detalhes, com todas as informações do registro: espécie, sexo, porte, idade, se é castrado e vacinado, a descrição e as datas de cadastro e de atualização."

**João Pedro:** clique em **Editar**, mude o **Status para Adotado** e clique em **Salvar alterações**.
> "Na edição o formulário já vem preenchido com os dados atuais. Mudei o status para Adotado e salvei: aparece a mensagem de sucesso e a data de atualização muda."

### Slide 12 · DEMO: Exclusão e responsividade (Samuel)
- Clique em **Excluir** → aparece o diálogo de confirmação → clique em **Cancelar**.
> "Antes de excluir, o sistema sempre pede confirmação. Se eu cancelar, nada acontece."
- Clique em **Excluir** de novo → **Excluir** no diálogo.
> "Confirmando, o registro é removido, aparece a mensagem de sucesso e voltamos para a listagem."
- (Opcional) F12 → ícone de celular (Ctrl+Shift+M) para mostrar a **versão mobile**.

### Slide 13 · Requisitos atendidos (Guilherme)
> "Aqui está o checklist do enunciado, todos os itens atendidos: as quatro páginas além do login, o login real no backend, serviços Angular, validação, mensagens, confirmação de exclusão, rotas protegidas, logout, o CRUD completo com camadas e DTOs, os códigos HTTP, o JPA com banco relacional, e o JWT com BCrypt."

**Demonstração de persistência (Társis no computador):** no Terminal 2, pare o backend (**Ctrl+C**) e rode `mvnw.cmd spring-boot:run` de novo. Atualize a página (F5): os dados continuam lá.
> "Reiniciamos o backend e os dados continuam salvos no PostgreSQL."

### Slide 14 · Qualidade (Ylo)
> "Para garantir que tudo funciona, escrevemos 28 testes automatizados no backend, entre testes unitários e testes de integração com MockMvc. Eles cobrem login, token inválido e expirado, o CRUD completo e os erros 400, 404 e 409. No frontend testamos o serviço de autenticação, o guard e o interceptor. Também validamos a persistência e deixamos o projeto fácil de rodar com três comandos."

### Slide 15 · Equipe (Ylo)
> "Esta é a divisão do trabalho: o Társis cuidou da infraestrutura, da integração e da autenticação no frontend; eu fiquei com a segurança, ou seja, Spring Security, JWT e BCrypt; o Guilherme com as entidades, o CRUD, os DTOs e o tratamento de erros; o Samuel com a listagem, os detalhes e a exclusão; e o João Pedro com o cadastro, a edição e os formulários."

### Slide 16 · Encerramento (Társis)
- No sistema, clique em **Sair** → volta para o login.
> "E para fechar, o botão Sair encerra a sessão e remove o token. O código está no GitHub, no link do slide, com o README explicando como rodar. Obrigado! Ficamos à disposição para perguntas."

---

## 3. Perguntas prováveis do professor (e respostas)

**Por que JWT e não sessão?**
> A API é *stateless*: o servidor não guarda sessão, e cada requisição traz o token assinado. Isso simplifica a escalabilidade e é o padrão para SPAs como o Angular.

**Onde o token expira e o que acontece?**
> A expiração é de 2 horas (`adotapet.jwt.expiracao` no `application.yml`). O backend responde 401 "Token expirado" e o Angular encerra a sessão e volta ao login. O frontend também agenda o logout automático no horário de expiração.

**Como a senha é guardada?**
> Com hash BCrypt (`PasswordEncoder`). No banco fica algo como `$2a$10$...`. Nenhum DTO de resposta tem o campo senha.

**Por que DTOs e não devolver a entidade direto?**
> Para controlar o que entra e o que sai da API (por exemplo, nunca expor a senha), validar a entrada com Bean Validation e desacoplar o modelo do banco do contrato da API.

**Onde fica a validação?**
> Nos dois lados: Bean Validation nos DTOs do backend (`@NotBlank`, `@Size`, `@Min`...) e Reactive Forms no Angular. Os erros 400 da API aparecem no campo correspondente do formulário.

**Como os dados persistem após reiniciar?**
> O PostgreSQL roda no Docker com um volume nomeado, e o Hibernate usa `ddl-auto: update`, ou seja, não apaga as tabelas. No perfil H2, os dados ficam em arquivo.

**Como as rotas são protegidas no Angular?**
> Com o `authGuard` (`CanActivateFn`), que verifica se existe um token válido e, se não existir, redireciona para `/login?returnUrl=...`. O `authInterceptor` adiciona o cabeçalho Bearer e trata o 401.

**E o CORS?**
> O backend libera a origem `http://localhost:4200` e o cabeçalho Authorization, configurável pela variável `CORS_ORIGEM`.

**Por que o cadastro de usuário não é o CRUD principal?**
> Porque o enunciado pede uma entidade do tema. O CRUD principal é o `Animal`; o cadastro de usuário existe só para criar contas de voluntário.
