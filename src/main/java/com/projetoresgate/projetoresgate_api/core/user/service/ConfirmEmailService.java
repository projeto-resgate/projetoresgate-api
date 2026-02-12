package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.ConfirmEmailUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.ConfirmEmailCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String tokenHash = TokenUtils.hashToken(command.token());
        EmailConfirmationToken confirmationToken = emailConfirmationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InternalException("Token inválido ou não encontrado."));

        if (confirmationToken.isExpired()) {
            throw new InternalException("O token expirou. Solicite um novo.");
        }

        User user = confirmationToken.getUser();
        if (user.isEmailVerified()) {
            return;
        }
        
        user.setIsEmailVerified(true);
        userRepository.save(user);

        emailConfirmationTokenRepository.delete(confirmationToken);
    }
}
