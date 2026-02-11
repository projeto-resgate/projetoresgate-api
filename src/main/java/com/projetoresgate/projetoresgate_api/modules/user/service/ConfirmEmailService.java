package com.projetoresgate.projetoresgate_api.modules.user.service;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.modules.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import com.projetoresgate.projetoresgate_api.modules.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.modules.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.ConfirmEmailUseCase;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.ConfirmEmailCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ConfirmEmailService implements ConfirmEmailUseCase {

    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final UserRepository userRepository;

    public ConfirmEmailService(EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                               UserRepository userRepository) {
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void handle(ConfirmEmailCommand command) {
        EmailConfirmationToken confirmationToken = emailConfirmationTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InternalException("Token inválido ou não encontrado."));

        if (!Objects.equals(confirmationToken.getUser().getId(), command.user().getId())) {
            throw new InternalException("Token inválido para este usuário.");
        }

        if (confirmationToken.isExpired()) {
            throw new InternalException("O token expirou. Solicite um novo.");
        }

        User user = confirmationToken.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);

        emailConfirmationTokenRepository.delete(confirmationToken);
    }
}
