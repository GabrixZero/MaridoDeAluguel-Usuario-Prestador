# DoesIt - Plataforma de Prestação de Serviços

## Visão Geral

Este é o repositório do **aplicativo móvel do Prestador de Serviços** do projeto DoesIt - uma plataforma mobile para gerenciamento de serviços prestados (encanamento, elétrica, pintura, marcenaria, limpeza, chaveiro, montagem).

O prestador pode:
- Registrar-se e gerenciar seu perfil
- Definir especialidades e preços
- Ficar online/offline para receber solicitações
- Aceitar, recusar ou gerenciar pedidos de clientes
- Avaliar clientes após conclusão de serviços
- Acompanhar histórico de serviços e ganhos

## Estrutura da Solução DoesIt

Este é um dos aplicativos da solução DoesIt:

1. **doesit-usuario** - App do Cliente (solicita serviços)
2. **doesit-prestador** - App do Prestador (oferece serviços) ← ESTE REPOSITÓRIO

Ambos consomem APIs do mesmo backend.

## Tecnologias Utilizadas

- **Linguagem**: Kotlin
- **Framework UI**: Jetpack Compose
- **Navegação**: Jetpack Navigation Compose
- **HTTP Client**: Retrofit + Okhttp3 + GSON
- **Build System**: Gradle (Kotlin DSL)
- **Android Min SDK**: 24 (Android 7.0)
- **Android Target SDK**: 36 (Android 15)

## Estrutura de Pastas

```
doesit-prestador/
├── app/
│   ├── build.gradle.kts          # Configuração de build
│   ├── proguard-rules.pro        # Regras de obfuscação
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/doesitprovider/
│       │   │   ├── data/           # Camada de dados
│       │   │   │   ├── model/      # DTOs e data classes
│       │   │   │   ├── network/    # Retrofit, API, SessionManager
│       │   │   │   └── repository/ # UserRepository, ServiceRepository
│       │   │   ├── ui/             # Camada de apresentação
│       │   │   │   ├── screens/    # Telas do app (12 telas)
│       │   │   │   ├── components/ # Componentes reutilizáveis
│       │   │   │   ├── navigation/ # Configuração de rotas
│       │   │   │   ├── theme/      # Cores, tipografia, tema
│       │   │   │   └── MainActivity.kt
│       │   │   └── R.kt            # Recursos (gerado)
│       │   └── res/                # Drawable, strings, values
│       ├── test/java/              # Unit tests
│       └── androidTest/java/       # UI tests
├── gradle/                         # Scripts gradle
├── build.gradle.kts               # Build root
├── settings.gradle.kts            # Configuração
└── gradlew, gradlew.bat          # Gradle wrapper

```

## Rodando o Projeto

### Pré-requisitos

- Android Studio Iguana ou superior
- JDK 11+
- Emulador/Device Android com API 24+
- Backend DoesIt rodando em `http://192.168.1.104:8080/`

### Passos para Build e Run

1. **Preparar ambiente**:
   ```bash
   # Clonar repositório
   git clone <url-do-repo>
   cd doesit-prestador
   
   # Se tiver local.properties, confirmar SDK local
   # (Android Studio configura automaticamente)
   ```

2. **No Android Studio**:
   - Abrir projeto na pasta `doesit-prestador`
   - Esperar Gradle sync terminar
   - Conectar device ou iniciar emulator
   - Run > Run 'app' (Shift + F10)

3. **Via Terminal**:
   ```bash
   # Build debug APK
   ./gradlew assembleDe

   
   # Instalar em device conectado
   ./gradlew installDebug
   
   # Rodar tests
   ./gradlew test
   ```

## Arquitetura e Fluxos

### Padrão de Arquitetura

Segue padrão em camadas:
- **Data Layer**: Models, Network (Retrofit), Repository (lógica de negócio)
- **UI Layer**: Screens (telas), Components (componentes reutilizáveis), Theme
- **Navigation**: Gerenciador de rotas entre telas

### Principais Fluxos de Negócio

1. **Autenticação**: Login → Recuperação de Senha → Registro
2. **Gerenciamento de Especialidades**: Adicionar, editar, remover categorias de serviço com preço
3. **Ciclo de Vida de Pedido**: Recebe → Aceita/Recusa → Inicia → Cancela/Completa → Avalia
4. **Status Online/Offline**: Alterna disponibilidade para novos pedidos
5. **Perfil e Configurações**: Editar dados, mudar senha, deletar account

### Modelos de Dados Principais

**ServiceRequestDTO** - Representa um pedido de serviço
- Estados: PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELED, REFUSED
- Tipos: SCHEDULED (agendado) ou INSTANT (agora)
- Dados: Cliente, Categoria, Preço, Endereço, Descrição, Data/Hora

**ProviderSpecialtyDTO** - Especialidade com preço do prestador
- Categorias: Encanamento, Elétrica, Pintura, Marcenaria, Limpeza, Chaveiro, Montagem
- Cada uma tem preço definido pelo prestador

**AuthResponse** - Dados do prestador autenticado
- Token JWT, ID, Nome, Email, CPF, Telefone, Rating, Contagem de avaliações

## Telas Principais (12 telas)

1. **Login** - Autenticação com recuperação de senha
2. **Registro** - Cadastro de novo prestador
3. **Home** - Tela principal com status online/offline e atalhos
4. **Detalhes do Serviço** - Gerenciamento de um pedido específico
5. **Histórico** - Lista de serviços já realizados/cancelados
6. **Especialidades** - Gerenciamento de categorias e preços
7. **Perfil** - Exibição e edição de dados pessoais
8. **Configurações** - Mudar senha, deletar conta, logout
9. **Recibos** - Saldo disponível e histórico de pagamentos
10. **Avaliações** - Avaliações recebidas de clientes
11. **Endereço** - Edição de endereço cadastrado
12. **Notificações** - Centro de notificações

## API Backend (Endpoints)

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registro
- `POST /api/auth/forgot-password` - Recuperar senha (etapa 1)
- `POST /api/auth/verify-code` - Verificar código (etapa 2)
- `POST /api/auth/reset-password` - Resetar senha (etapa 3)

### Usuário (requer token)
- `GET /api/users/me` - Dados do usuário
- `PUT /api/users/me` - Atualizar perfil
- `PUT /api/users/me/password` - Mudar senha
- `DELETE /api/users/me` - Deletar conta

### Prestador (requer token)
- `PUT /api/providers/status` - Atualizar online/offline

### Especialidades (requer token)
- `GET /api/providers/specialties` - Listar especialidades
- `POST /api/providers/specialties` - Criar/editar especialidade
- `DELETE /api/providers/specialties/{categoryId}` - Remover especialidade

### Pedidos/Serviços (requer token)
- `GET /api/requests/my` - Listar todos os pedidos
- `GET /api/requests/{id}` - Detalhes de um pedido
- `PUT /api/requests/{id}/accept` - Aceitar pedido
- `PUT /api/requests/{id}/refuse` - Recusar pedido
- `PUT /api/requests/{id}/start` - Iniciar pedido
- `PUT /api/requests/{id}/cancel` - Cancelar pedido

### Avaliações (requer token)
- `POST /api/ratings/user` - Avaliar cliente
- `GET /api/ratings/my-received` - Avaliações recebidas

### Endereços (requer token)
- `GET /api/addresses` - Listar endereços

## Regras de Negócio Principais

### Ciclo de Vida do Serviço

1. **PENDING** (Aguardando resposta)
   - Ações: Aceitar ou Recusar
   - Timeout automático se não responder

2. **ACCEPTED** (Aceito)
   - Ações: Iniciar serviço
   - Regra: Se SCHEDULED, esperar 10 minutos antes de iniciar

3. **IN_PROGRESS** (Em andamento)
   - Ações: Cancelar serviço
   - Bloqueado: Não pode recusar nesta etapa

4. **COMPLETED** (Concluído)
   - Ações: Avaliar cliente (obrigatório para sair da fila)
   - 1-5 estrelas com comentário opcional

5. **CANCELED** (Cancelado)
   - Sem ações adicionais
   - Vai direto para histórico

6. **REFUSED** (Recusado)
   - Sem ações adicionais
   - Pedido fica disponível para outro prestador

### Regras de Especialidades

- Prestador deve ter pelo menos 1 especialidade para receber pedidos
- 7 categorias disponíveis: Encanamento, Elétrica, Pintura, Marcenaria, Limpeza, Chaveiro, Montagem
- Cada especialidade tem preço definido pelo prestador
- Mudança de preço é imediata

### Status Online/Offline

- Apenas online recebe novas solicitações
- Pode gerenciar pedidos já aceitos mesmo offline
- Alterna no botão na Home

## Componentes Reutilizáveis

- **DoesItButton** - Botão primário com loading e arrow
- **DoesItTextField** - Campo de entrada com máscaras
- **ErrorBanner** - Banner de erro auto-dismiss
- **SuccessBanner** - Banner de sucesso auto-dismiss
- **DoesItBottomNavBar** - Navegação inferior
- **StatusBadge** - Badge colorida de status
- **QuickAccessCard** - Card de acesso rápido

## SessionManager (Estado Global)

Singleton que gerencia estado da sessão:
- Token JWT
- Dados do usuário (ID, Nome, Email, CPF, Telefone, etc)
- Rating e contagem de avaliações (reativo)
- Status online (reativo)

## Segurança

- **JWT Token**: Enviado em header `Authorization: Bearer <token>`
- **Validações**: Client-side (formato) + Server-side (lógica crítica)
- **Senhas**: Hash no servidor, requisitos mínimos (8+ chars, maiúscula, minúscula, número, símbolo)
- **HTTPS**: Ativo em produção, HTTP permitido em dev

## Tratamento de Erros

- Mensagens de erro específicas em banner vermelho
- Auto-dismiss após 5 segundos
- Retry habilitado em botões
- Timeout de conexão: 15 segundos

## Performance

- Lazy loading nas listas (LazyColumn)
- Coroutines async (UI não bloqueia)
- Recomposição otimizada com remember/mutableStateOf

## Próximos Passos

Possíveis expansões:
- WebSocket para real-time updates
- Mapa com geolocalização de clientes
- Chat integrado no app
- Sistema de pagamento (Stripe/PagSeguro)
- Portfólio de fotos de trabalhos
- Dashboard de analytics (ganhos, serviços)
- Offline mode com Room database
- Agendamento recorrente de serviços

## Troubleshooting

| Problema | Solução |
|----------|---------|
| "Sem conexão" | Verifique se backend rodando em http://192.168.1.104:8080 |
| APK não instala | Min SDK 24 (Android 7.0) requerido |
| Botões não respondem | Reiniciar app ou verificar logs (adb logcat) |
| Notificações não chegam | Habilitar notificações nas configurações do Android |

## Documentação Adicional

Para documentação técnica detalhada com todos os fluxos, endpoints, modelos de dados, regras de negócios e considerações de arquitetura, consulte **README_COMPLETO.md** neste repositório.

Ele contém:
- Especificação completa de cada fluxo
- Diagramas textuais de ciclos de vida
- DTOs com todos os campos
- Tratamento de erros específico
- Referências de código-fonte
- Arquitetura em detalhes

---

**Versão**: 1.0  
**Ultima Atualização**: 2024
