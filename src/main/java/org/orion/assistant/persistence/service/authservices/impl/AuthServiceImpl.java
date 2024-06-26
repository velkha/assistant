package org.orion.assistant.persistence.service.authservices.impl;

import org.orion.assistant.enums.Role;
import org.orion.assistant.exception.custom.bbdd.IncorrectPasswordException;
import org.orion.assistant.exception.custom.bbdd.InvalidDataException;
import org.orion.assistant.exception.custom.bbdd.UserAlreadyExistException;
import org.orion.assistant.exception.custom.bbdd.UserNotFoundException;
import org.orion.assistant.persistence.dao.auth.AuthResponse;
import org.orion.assistant.persistence.dao.auth.SignInReq;
import org.orion.assistant.persistence.dao.auth.SignUpReq;
import org.orion.assistant.persistence.model.User;
import org.orion.assistant.persistence.repositories.UserRepository;
import org.orion.assistant.persistence.service.authservices.AuthService;
import org.orion.assistant.persistence.service.authservices.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the AuthService interface
 * {@link org.orion.assistant.persistence.service.authservices.AuthService}
 */
@Service
public class AuthServiceImpl implements AuthService{
    private  UserRepository userRepository;
    private  PasswordEncoder passwordEncoder;
    private  JwtService jwtService;
    private  AuthenticationManager authenticationManager;

    /**
     * Constructor for the AuthServiceImpl class
     * @param userRepository - UserRepository object
     * @param passwordEncoder - PasswordEncoder object
     * @param jwtService - JwtService object
     * @param authenticationManager - AuthenticationManager object
     */
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * {@link org.orion.assistant.persistence.service.authservices.AuthService#signUp(SignUpReq)}
     * @param request - SignUpReq object
     * @return AuthResponse object
     * @throws UserAlreadyExistException 
     * @throws InvalidDataException 
     */
    @Override
    public AuthResponse signUp(SignUpReq request) throws UserAlreadyExistException, InvalidDataException {
        if (request.getUsername() == null || request.getEmail() == null || request.getPassword() == null
                || request.getUsername().equals("") || request.getEmail().equals("") || request.getPassword().equals("")){
            throw new InvalidDataException("User with given username or email already exists");
        }

        User existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyExistException("User with given username or email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwt).build();
    }

    /**
     * {@link org.orion.assistant.persistence.service.authservices.AuthService#signIn(SignInReq)}
     * @param request - SignInReq object
     * @return AuthResponse object
     * @throws UserNotFoundException 
     * @throws IncorrectPasswordException 
     * @throws InvalidDataException 
     */
    @Override
    public AuthResponse signIn(SignInReq request) throws UserNotFoundException, IncorrectPasswordException, InvalidDataException {
        if (request.getUsername() == null || request.getPassword() == null
                || request.getUsername().equals("") || request.getPassword().equals("")){
            throw new InvalidDataException("User with given username or email already exists");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new UserNotFoundException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Invalid email or password");
        }

        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwt).build();
    }
}
