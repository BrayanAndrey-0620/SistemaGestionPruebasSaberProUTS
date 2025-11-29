package com.saberpro.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saberpro.sistema.entity.EstadisticasPuntaje;
import com.saberpro.sistema.entity.ResultadoSaberPro;
import com.saberpro.sistema.repository.ResultadoSaberProRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ResultadoSaberProService {

    @Autowired
    private ResultadoSaberProRepository resultadoRepository;
    @Autowired
    private BeneficioService beneficioService; // Necesitas inyectar el servicio de beneficios

    public List<ResultadoSaberPro> obtenerTodosResultados() {
        return resultadoRepository.findAll();
    }

    public Optional<ResultadoSaberPro> obtenerResultadoPorId(Long id) {
        return resultadoRepository.findById(id);
    }

    public ResultadoSaberPro guardarResultado(ResultadoSaberPro resultado) {
        return resultadoRepository.save(resultado);
    }

    public void eliminarResultado(Long id) {
        resultadoRepository.deleteById(id);
    }

    public List<ResultadoSaberPro> obtenerResultadosPorEstudianteId(Long estudianteId) {
        return resultadoRepository.findByEstudianteId(estudianteId);
    }

    public Optional<ResultadoSaberPro> obtenerResultadoPorDocumentoEstudiante(String documento) {
        return resultadoRepository.findByEstudianteNumeroDocumento(documento);
    }

    public List<ResultadoSaberPro> obtenerResultadosPorTipoPrueba(String tipoPrueba) {
        return resultadoRepository.findByTipoPruebaOrderByPuntajeDesc(tipoPrueba);
    }

    public Double obtenerPromedioPorTipoPrueba(String tipoPrueba) {
        return resultadoRepository.findPromedioPuntajeByTipoPrueba(tipoPrueba);
    }

    public long contarTotalResultados() {
        return resultadoRepository.count();
    }

    public long contarEstudiantesConResultado() {
        return resultadoRepository.findAll().stream()
                .map(resultado -> resultado.getEstudiante().getId())
                .distinct()
                .count();
    }

    public List<Object[]> obtenerTop10Resultados() {
        // Implementación para obtener top 10 resultados
        return resultadoRepository.findByTipoPruebaOrderByPuntajeDesc("SABER_PRO")
                .stream()
                .limit(10)
                .map(resultado -> new Object[]{
                    resultado.getEstudiante().getPrimerNombre() + " " + resultado.getEstudiante().getPrimerApellido(),
                    resultado.getPuntajeGlobal(),
                    resultado.getTipoPrueba()
                })
                .toList();
    }
    
    /**
     * Cuenta el número total de exámenes presentados para un tipo de prueba específico.
     */
    public Long contarResultadosPorTipoPrueba(String tipoPrueba) {
        // Utiliza el método del repositorio que hemos añadido:
        return resultadoRepository.countByTipoPrueba(tipoPrueba);
    }

    /**
     * Cuenta cuántos resultados cumplen el criterio de aprobación.
     */
    public Long contarAprobadosPorTipoPrueba(String tipoPrueba) {
        // Utiliza el método del repositorio que hemos añadido (con la lógica de puntajes de corte):
        return resultadoRepository.countAprobadosByTipoPrueba(tipoPrueba); 
    }

    /**
     * Cuenta cuántos resultados obtuvieron un beneficio.
     */
    public Long contarConBeneficiosPorTipoPrueba(String tipoPrueba) {
    	// 1. Obtener todos los resultados de ese tipo de prueba
        List<ResultadoSaberPro> resultados = resultadoRepository.findByTipoPruebaOrderByPuntajeDesc(tipoPrueba);

        // 2. Calcular el beneficio para cada resultado (la lógica que ya tienes en el controlador)
        resultados.forEach(resultado -> {
            if ("SABER_TT".equals(resultado.getTipoPrueba())) {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberTyt(resultado.getPuntajeGlobal()));
            } else {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberPro(resultado.getPuntajeGlobal()));
            }
        });

        // 3. Contar aquellos que tienen un beneficio calculado
        return resultados.stream()
                .filter(r -> r.getBeneficio() != null && 
                             (r.getBeneficio().getNotaAsignada() != null || r.getBeneficio().getPorcentajeBeca() > 0))
                .count();
    }
    
    public void calcularDistribucionPorNivel(String tipoPrueba, EstadisticasPuntaje estadisticas) {
        Long total = estadisticas.getTotal();
        if (total == null || total == 0) {
            return; // No hay datos para calcular
        }

        // Obtener conteos por nivel desde el repositorio: ["NIVEL4", 50]
        List<Object[]> conteosPorNivel = resultadoRepository.countByTipoPruebaAndGroupByNivelGlobal(tipoPrueba);

        // Mapear los conteos para un acceso fácil
        Map<String, Long> mapaConteo = new HashMap<>();
        for (Object[] row : conteosPorNivel) {
            if (row[0] != null && row[1] != null) {
                mapaConteo.put((String) row[0], (Long) row[1]);
            }
        }

        // Extraer y asignar los valores (Asegúrate de que tus valores de nivelGlobal sean exactamente "NIVEL1", "NIVEL2", etc.)
        
        // Nivel 4 (Excelente)
        Long nivel4 = mapaConteo.getOrDefault("NIVEL4", 0L);
        Double porcentajeNivel4 = (nivel4 * 100.0) / total;
        estadisticas.setNivel4(nivel4);
        estadisticas.setPorcentajeNivel4(porcentajeNivel4);

        // Nivel 3 (Bueno)
        Long nivel3 = mapaConteo.getOrDefault("NIVEL3", 0L);
        Double porcentajeNivel3 = (nivel3 * 100.0) / total;
        estadisticas.setNivel3(nivel3);
        estadisticas.setPorcentajeNivel3(porcentajeNivel3);
        
        // Nivel 2 (Básico)
        Long nivel2 = mapaConteo.getOrDefault("NIVEL2", 0L);
        Double porcentajeNivel2 = (nivel2 * 100.0) / total;
        estadisticas.setNivel2(nivel2);
        estadisticas.setPorcentajeNivel2(porcentajeNivel2);
        
        // Nivel 1 (Insuficiente)
        Long nivel1 = mapaConteo.getOrDefault("NIVEL1", 0L);
        Double porcentajeNivel1 = (nivel1 * 100.0) / total;
        estadisticas.setNivel1(nivel1);
        estadisticas.setPorcentajeNivel1(porcentajeNivel1);
}
}
