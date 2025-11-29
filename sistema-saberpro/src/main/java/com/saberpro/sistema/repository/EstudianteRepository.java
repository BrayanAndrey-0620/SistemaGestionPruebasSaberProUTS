package com.saberpro.sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.saberpro.sistema.entity.Estudiante;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
    Optional<Estudiante> findByNumeroRegistro(String numeroRegistro);
    List<Estudiante> findByPrimerApellidoContainingIgnoreCase(String apellido);
    
    @Query("SELECT e FROM Estudiante e WHERE e.primerNombre LIKE %:nombre% OR e.segundoNombre LIKE %:nombre%")
    List<Estudiante> findByNombreContaining(String nombre);
}