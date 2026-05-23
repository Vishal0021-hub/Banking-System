package com.banking.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(max = 255, message = "Address too long")
    private String address;

    @Size(max = 50, message = "City name too long")
    private String city;

    @Size(max = 50, message = "State name too long")
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN number format")
    private String panNumber;

    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid Aadhar number")
    private String aadharNumber;
}
