package com.agrocortex.auth.application.login;

public interface LoginUseCase {

    LoginResult execute(LoginCommand command);
}
