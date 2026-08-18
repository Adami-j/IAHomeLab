package fr.lab.iahomelab.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

public enum UserRole {
    USER,
    ADMIN
}