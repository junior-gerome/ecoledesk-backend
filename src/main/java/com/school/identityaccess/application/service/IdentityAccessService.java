package com.school.identityaccess.application.service;

import com.school.identityaccess.application.dto.AuthTokenResponse;
import com.school.identityaccess.application.dto.AuthenticateUserCommand;
import com.school.identityaccess.application.dto.RegisterUserCommand;
import com.school.identityaccess.application.dto.UserSummaryResponse;
import com.school.identityaccess.application.port.out.IssuedToken;
import com.school.identityaccess.application.port.out.PasswordHasher;
import com.school.identityaccess.application.port.out.ProfileRepository;
import com.school.identityaccess.application.port.out.TokenIssuer;
import com.school.identityaccess.application.port.out.UserAccountRepository;
import com.school.identityaccess.domain.model.Person;
import com.school.identityaccess.domain.model.Profile;
import com.school.identityaccess.domain.model.UserAccount;
import com.school.shared.domain.DuplicateResourceException;
import com.school.shared.domain.ResourceNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityAccessService {
    private final UserAccountRepository userAccountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    public IdentityAccessService(
            UserAccountRepository userAccountRepository,
            ProfileRepository profileRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer
    ) {
        this.userAccountRepository = userAccountRepository;
        this.profileRepository = profileRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public UserSummaryResponse register(RegisterUserCommand command) {
        String username = normalize(command.email());
        if (userAccountRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("A user already exists with this email");
        }

        Profile profile = profileRepository.findActiveByCode(command.effectiveProfileCode())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + command.effectiveProfileCode()));

        Person person = Person.create(command.firstName(), command.lastName(), username, command.phoneNumber());
        UserAccount user = UserAccount.create(username, passwordHasher.hash(command.rawPassword()), person);
        user.assignProfile(profile);

        return toSummary(userAccountRepository.save(user));
    }

    @Transactional
    public AuthTokenResponse authenticate(AuthenticateUserCommand command) {
        String username = normalize(command.email());
        UserAccount user = userAccountRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive() || !passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        user.markLogin(clock);
        IssuedToken token = tokenIssuer.issue(user);
        return new AuthTokenResponse(token.value(), token.expiresAt(), toSummary(user));
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getByUsername(String username) {
        return userAccountRepository.findByUsername(normalize(username))
                .map(this::toSummary)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listUsers() {
        return userAccountRepository.findAll().stream().map(this::toSummary).toList();
    }

    private UserSummaryResponse toSummary(UserAccount user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getPerson().fullName(),
                user.getPerson().getEmail(),
                user.isActive(),
                user.getLastLoginAt(),
                user.authorityCodes()
        );
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidCredentialsException();
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
