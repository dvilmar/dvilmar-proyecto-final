package com.bookmycut.repositories;

import com.bookmycut.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    
    /**
     * Busca un usuario por username o email.
     * Útil para autenticación cuando el usuario puede usar cualquiera de los dos.
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    @EntityGraph(attributePaths = {"services"})
    Optional<User> findWithServicesByUserId(Long userId);
    
    java.util.List<User> findByRoleAndActive(User.Role role, Boolean active);
    
    /**
     * Inserta un registro en la tabla clientes después de crear un usuario CLIENTE.
     * @param usuarioId ID del usuario cliente
     * @param phone Teléfono del cliente (puede ser null)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO clientes (usuario_id, phone) VALUES (:usuarioId, :phone) ON DUPLICATE KEY UPDATE phone = :phone", nativeQuery = true)
    void insertClienteRecord(@Param("usuarioId") Long usuarioId, @Param("phone") String phone);
    
    /**
     * Inserta un registro en la tabla estilistas después de crear un usuario ESTILISTA.
     * @param usuarioId ID del usuario estilista
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO estilistas (usuario_id) VALUES (:usuarioId)", nativeQuery = true)
    void insertEstilistaRecord(@Param("usuarioId") Long usuarioId);
}






