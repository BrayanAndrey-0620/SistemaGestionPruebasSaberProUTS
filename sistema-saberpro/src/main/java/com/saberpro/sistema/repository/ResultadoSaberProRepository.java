package com.saberpro.sistema.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.saberpro.sistema.entity.ResultadoSaberPro;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoSaberProRepository extends JpaRepository<ResultadoSaberPro, Long> {
    List<ResultadoSaberPro> findByEstudianteId(Long estudianteId);
    Optional<ResultadoSaberPro> findByEstudianteNumeroDocumento(String numeroDocumento);
    
    @Query("SELECT r FROM ResultadoSaberPro r WHERE r.tipoPrueba = :tipoPrueba ORDER BY r.puntajeGlobal DESC")
    List<ResultadoSaberPro> findByTipoPruebaOrderByPuntajeDesc(String tipoPrueba);
    
    @Query("SELECT AVG(r.puntajeGlobal) FROM ResultadoSaberPro r WHERE r.tipoPrueba = :tipoPrueba")
    Double findPromedioPuntajeByTipoPrueba(String tipoPrueba);
    
 // 1. Contar el número total de resultados (exámenes) por tipo de prueba.
    Long countByTipoPrueba(String tipoPrueba);

    /**
     * Cuenta los resultados que cumplen con el criterio de 'Aprobado'.
     * Asunción: SABER_PRO >= 120 y SABER_TT >= 80.
     * **Ajusta los umbrales (120/80) si tus reglas de negocio son diferentes.**
     */
    @Query("SELECT COUNT(r) FROM ResultadoSaberPro r WHERE r.tipoPrueba = :tipoPrueba AND r.puntajeGlobal >= " +
           "CASE WHEN r.tipoPrueba = 'SABER_PRO' THEN 120 " +
           "     WHEN r.tipoPrueba = 'SABER_TT' THEN 80 " +
           "     ELSE 0 END") // El 'ELSE 0' es un caso por defecto, aunque no debería ocurrir.
    Long countAprobadosByTipoPrueba(String tipoPrueba);
    
    /**
     * Cuenta los resultados que obtuvieron algún beneficio.
     * Asunción: La entidad ResultadoSaberPro tiene una relación/campo 'beneficio' que no es NULL si aplica.
     * **Ajusta 'r.beneficio IS NOT NULL' si tu campo es diferente (e.g., r.aplicaBeneficio = TRUE).**
     */
    
    @Query("SELECT r.nivelGlobal, COUNT(r) FROM ResultadoSaberPro r WHERE r.tipoPrueba = :tipoPrueba GROUP BY r.nivelGlobal")
    List<Object[]> countByTipoPruebaAndGroupByNivelGlobal(String tipoPrueba);
}