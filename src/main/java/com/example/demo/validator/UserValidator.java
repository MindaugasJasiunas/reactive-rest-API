package com.example.demo.validator;

import com.example.demo.User;

import java.util.function.Function;

public interface UserValidator extends Function<User, UserValidator.ValidationResult> {
    enum ValidationResult{
        SUCCESS("Success"), USERNAME_NOT_VALID("Username not valid"), FIRSTNAME_NOT_VALID("First name not valid"), LASTNAME_NOT_VALID("Last name not valid"), PASSWORD_NOT_VALID("Password not valid");
        private String reason;
        ValidationResult(String reason){
            this.reason = reason;
        }
        public String getReason(){
            return this.reason;
        }
    }

    // validations
    static UserValidator isUsernameValid(){
        return user -> (user.getUsername() != null && user.getUsername().trim().length() >= 6) ? ValidationResult.SUCCESS : ValidationResult.USERNAME_NOT_VALID;
    }

    static UserValidator isFirstNameValid(){
        return user -> (user.getFirstName() != null && !user.getFirstName().trim().equals("")) ? ValidationResult.SUCCESS : ValidationResult.FIRSTNAME_NOT_VALID;
    }

    static UserValidator isLastNameValid(){
        return user -> (user.getLastName() != null && !user.getLastName().trim().equals("")) ? ValidationResult.SUCCESS : ValidationResult.LASTNAME_NOT_VALID;
    }

    static UserValidator isPasswordValid(){
        return user -> (user.getPassword() != null && user.getPassword().trim().length() >= 8) ? ValidationResult.SUCCESS : ValidationResult.PASSWORD_NOT_VALID;
    }

    //at the end chain them together
    default UserValidator and(UserValidator other){
        return user -> {
            ValidationResult result = this.apply(user);
            // apply other validation or return error(result)
            return result.equals(ValidationResult.SUCCESS) ? other.apply(user) : result;
        };
    }
}
