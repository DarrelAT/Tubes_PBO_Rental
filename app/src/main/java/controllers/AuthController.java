package controllers;

import models.User;
import repository.UserRepository;

public class AuthController {
    private UserRepository userRepo;
    private User currentUser;

    public AuthController() {
        this.userRepo = new UserRepository();
    }

    public boolean login(String username, String password) {
        User user = userRepo.getByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}