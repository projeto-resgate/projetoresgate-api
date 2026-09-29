# Testes

Como os testes são organizados e executados. Para o porquê da estratégia, veja
[ADR 004](adr/004-estrategia-testes.md) e [ADR 007](adr/007-testcontainers-postgres.md).

## Panorama

| Tipo                | Anotação                                   | Quantidade | Sobe contexto Spring? | Toca banco?           |
|---------------------|--------------------------------------------|------------|-----------------------|-----------------------|
| Unitário            | `@ExtendWith(MockitoExtension.class)`      | 42         | Não                   | Não                   |
| Controller          | `@WebMvcTest`                              | 5          | Parcial               | Não                   |
| Integração de dados | `@DataJpaTest` + `PostgresIntegrationTest` | 3          | Parcial               | Sim, em Postgres real |

Total: 69 classes de teste, 377 testes. Mais a classe base `PostgresIntegrationTest`, que não tem teste próprio.

O projeto **não usa `@SpringBootTest`**. Ele sobe o contexto inteiro sem necessidade e deixa a
suíte lenta. `@WebMvcTest` carrega só a web layer; `@DataJpaTest` carrega só a persistência.

## Como rodar

```bash
./mvnw -o test                                    # suíte completa, offline (mais rápido)
./mvnw test                                      # suíte completa, resolvendo dependências
./mvnw test -Dtest=FamilyGroupServiceIntegrationTest   # uma classe
./mvnw test -Dtest=FamilyGroupServiceIntegrationTest#paginar*  # um método
```

Use `-o` (offline) no dia a dia. Sem ele, o Maven tenta resolver dependências na rede e um teste
de 20s vira 2 minutos. Só rode sem `-o` depois de adicionar dependência nova, para popular o
cache.

**Docker precisa estar no ar** para as 3 classes de integração de dados. Elas sobem um Postgres
via Testcontainers.

## Teste unitário

Testa a lógica de um service, entidade ou componente isolado. Não sobe o Spring.

```java

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateFamilyGroupService - Test")
class CreateFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @InjectMocks
    private CreateFamilyGroupService service;

    @Test
    @DisplayName("Deve criar o grupo com o friendly id informado")
    void handle_ShouldCreateGroup() {
        // Arrange
        when(repository.save(any(FamilyGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        FamilyGroup group = service.handle(new CreateFamilyGroupCommand(/* ... */));

        // Assert
        assertEquals("FAM-1", group.getFriendlyId());
        verify(repository).save(any(FamilyGroup.class));
    }
}
```

Convenções:

- Classe: `{Classe}Test`.
- Método: `metodoDeveComportamento_quandoCondicao`.
- `@DisplayName` em português, descrevendo o **comportamento**, não o método.
- Arrange/Act/Assert separado por comentário quando o teste é longo.

**NÃO escreva teste que só verifica mock.** Um teste que confirma que o Mockito devolveu o que
você mandou não testa nada. Asserte o que importa:

- campos devolvidos pelo service;
- exceção lançada, com `assertThrows`;
- `verify` de efeito colateral real (`save`, `linkNaturalPerson`);
- **em controller, o JSON de saída e os campos que não devem aparecer**, com
  `jsonPath("$.content[0].email").doesNotExist()`.

## Teste de controller

Usa `@WebMvcTest` com o controller específico e `MockMvc`. Os use cases são `@Mock`.

```java

@WebMvcTest(FamilyGroupController.class)
@Import(SecurityConfigurations.class)
@DisplayName("FamilyGroupController - Test")
class FamilyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateFamilyGroupUseCase createUseCase;
    // ... um mock por use case

    @Test
    @WithMockCustomUser
    @DisplayName("POST /family-group - Deve retornar 201 Created")
    void create_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/family-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/family-group/" + id))
                .andExpect(jsonPath("$.friendlyId").value("FAM-1"));
    }
}
```

Pontos que costumam quebrar:

- **O `@Import(SecurityConfigurations.class)` é obrigatório**, senão a requisição volta 403 e o
  teste falha sem motivo claro.
- **Use `@WithMockCustomUser`** para simular o usuário autenticado. O helper fica em
  `src/test/java/com/projetoresgate/projetoresgate_api/config/security/`. Sem ele, tudo cai em 401.
- **Rotas CSRF precisam de `.with(csrf())`** quando o teste não é de GET.

## Teste de integração de dados

Estende `PostgresIntegrationTest`, que sobe um container PostgreSQL compartilhado e conecta via
`@DynamicPropertySource`. Com isso, o schema dos testes é o de produção, com as migrations do
Flyway aplicadas de verdade.

```java

@DataJpaTest
@DisplayName("FamilyGroupService - Integração")
class FamilyGroupServiceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // A sequence vem da migration V011. Precisa ser reiniciada porque, no Postgres,
        // nextval não reverte com o rollback da transação de teste e vazaria entre os testes.
        entityManager.createNativeQuery("ALTER SEQUENCE family_group_friendly_id_seq RESTART WITH 1")
                .executeUpdate();
    }
}
```

O `@DataJpaTest` faz rollback automático ao fim de cada método, o que dá isolamento. Mas
`nextval` **não reverte** com o rollback no Postgres, porque sequence é não transacional. Daí o
reset explícito.

### O que testar aqui

Use para o que o H2 mentiria:

- JPQL e query nativa contra o dialeto real;
- paginação com `join`, incluindo o `count`;
- constraint de banco e migration;
- `select for update`, se um dia existir.

Não re-teste aqui a lógica de negócio que o teste unitário já cobre. Integração é cara; use para
o que só o banco real revela.

## Relatório de cobertura

```bash
./mvnw test
open target/site/jacoco/index.html
```

O Jacoco está configurado no `pom.xml` com `prepare-agent` e `report`. Não há gate de cobertura
mínima no build: passar a cobertura é responsabilidade de quem revisa, não da CI.

## O que não fazer

- **Não reintroduzir H2.** O H2 não está mais no `pom.xml` e não valida dialeto Postgres. Se um
  teste novo precisar de banco, ele estende `PostgresIntegrationTest`.
- **Não use `@SpringBootTest` sem necessidade.** Sobe o contexto inteiro e deixa a suíte lenta.
- **Não escreva teste que só verifica mock.** Ver acima.
- **Não use `System.out.println` para validar.** Use `Assertions`.
- **Não deixe teste falhando.** Se falhou, o build quebra e a causa precisa ser resolvida, não
  comentada com `@Disabled`.
- **Não dependa de dados pré-existentes.** O teste cria o que precisa.

## Erros que custam tempo

O compilador incremental do Maven pode reportar `test-compile` como verde usando classes
`.class` obsoletas depois de uma mudança de assinatura. Se um teste deveria estar quebrado e
`test-compile` passa, rode `./mvnw -o clean test-compile` para confirmar.
