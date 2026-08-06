package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.ConfirmEmailUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.ConfirmEmailCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmEmailService implements ConfirmEmailUseCase {

    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final NaturalPersonRepository naturalPersonRepository;

    public ConfirmEmailService(EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                               NaturalPersonRepository naturalPersonRepository) {
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.naturalPersonRepository = naturalPersonRepository;
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

        NaturalPerson person = confirmationToken.getNaturalPerson();
        if (person.isEmailVerified()) {
            return;
        }

        person.confirmEmail();
        naturalPersonRepository.save(person);

        emailConfirmationTokenRepository.delete(confirmationToken);
    }
}
