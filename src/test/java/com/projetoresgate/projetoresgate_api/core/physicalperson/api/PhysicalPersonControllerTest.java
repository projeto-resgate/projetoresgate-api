package com.projetoresgate.projetoresgate_api.core.physicalperson.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.*;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhysicalPersonController.class)
@Import(SecurityConfigurations.class)
@DisplayName("PhysicalPersonController - Test")
class PhysicalPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePhysicalPersonUseCase createUseCase;

    @MockitoBean
    private UpdatePhysicalPersonUseCase updateUseCase;

    @MockitoBean
    private SoftDeletePhysicalPersonUseCase softDeleteUseCase;

    @MockitoBean
    private FindPhysicalPersonByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindPhysicalPersonByUserIdUseCase findByUserIdUseCase;

    @MockitoBean
    private SearchPhysicalPersonUseCase searchUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /physical-person/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void update_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePhysicalPersonCommand command = new UpdatePhysicalPersonCommand(
                null, "Updated Name", "newnick", null, "51086174968", null, null, null, null);

        User user = User.create("test@test.com", "pass1234", "Name", null);
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);

        when(updateUseCase.handle(any())).thenReturn(person);

        mockMvc.perform(put("/physical-person/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /physical-person/{id} - Deve retornar 204 No Content ao deletar com sucesso")
    void delete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/physical-person/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
