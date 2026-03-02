package ar.uba.fi.ingsoft1.product_example.user;

import jakarta.validation.constraints.Size;

import static ar.uba.fi.ingsoft1.product_example.user.UserConstants.MIN_PASSWORD_LENGHT;

record UserResetPasswordDTO (
        @Size(min = MIN_PASSWORD_LENGHT, max = MIN_PASSWORD_LENGHT) String password

) {

}

