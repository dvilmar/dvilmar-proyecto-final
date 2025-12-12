package com.bookmycut.controller;

import com.bookmycut.dto.AuthResponse;
import com.bookmycut.dto.LoginRequest;
import com.bookmycut.dto.RegisterRequest;
import com.bookmycut.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Authentication", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema. Los usuarios pueden ser CLIENTE, ESTILISTA o ADMINISTRADOR. " +
                    "Los clientes deben proporcionar un teléfono. Retorna un token JWT para autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o rol no válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email ya está registrado",
                    content = @Content
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con username y contraseña. Retorna un token JWT que debe ser incluido " +
                    "en el header 'Authorization: Bearer <token>' para acceder a endpoints protegidos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas o usuario inactivo",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Cerrar sesión",
            description = "Endpoint para cerrar sesión. En una implementación completa, invalidaría el token. " +
                    "Actualmente el logout se maneja en el frontend eliminando el token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // En una implementación real, podrías invalidar el token aquí
        // Por ahora, el logout se maneja en el frontend eliminando el token
        return ResponseEntity.ok().build();
    }
}
