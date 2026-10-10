package com.schemesathi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        private String password;

        private String phone;

        public RegisterRequest() {}

        public RegisterRequest(String fullName, String email, String password, String phone) {
            this.fullName = fullName;
            this.email = email;
            this.password = password;
            this.phone = phone;
        }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
        private String role;
        private boolean hasProfile;

        public AuthResponse() {}

        public AuthResponse(String token, String email, String fullName, String role, boolean hasProfile) {
            this.token = token;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.hasProfile = hasProfile;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String token;
            private String email;
            private String fullName;
            private String role;
            private boolean hasProfile;

            public Builder token(String token) { this.token = token; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder fullName(String fullName) { this.fullName = fullName; return this; }
            public Builder role(String role) { this.role = role; return this; }
            public Builder hasProfile(boolean hasProfile) { this.hasProfile = hasProfile; return this; }

            public AuthResponse build() {
                return new AuthResponse(token, email, fullName, role, hasProfile);
            }
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public boolean isHasProfile() { return hasProfile; }
        public void setHasProfile(boolean hasProfile) { this.hasProfile = hasProfile; }
    }
}
