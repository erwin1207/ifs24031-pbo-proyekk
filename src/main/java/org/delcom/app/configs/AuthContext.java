package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuthContext {
    private User authUser;

    public User getAuthUser() {
        return authUser;
    }

    public void setAuthUser(User authUser) {
        this.authUser = authUser;
    }

    public boolean isAuthenticated() {
        return authUser != null;
    }
}
