package com.saberpro.sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.saberpro.sistema.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findByRol(String rol);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'ESTUDIANTE'")
    List<Usuario> findAllEstudiantes();
    
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'COORDINACION'")
    List<Usuario> findAllCoordinadores();
}
