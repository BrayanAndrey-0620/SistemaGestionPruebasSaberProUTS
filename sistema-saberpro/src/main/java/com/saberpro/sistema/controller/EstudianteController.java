package com.saberpro.sistema.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.saberpro.sistema.entity.Beneficio;
import com.saberpro.sistema.entity.Estudiante;
import com.saberpro.sistema.entity.ResultadoSaberPro;
import com.saberpro.sistema.service.BeneficioService;
import com.saberpro.sistema.service.EstudianteService;
import com.saberpro.sistema.service.ResultadoSaberProService;

import java.util.*;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private ResultadoSaberProService resultadoService;

    @Autowired
    private BeneficioService beneficioService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        // Obtener resultados del estudiante
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        
        // Calcular estadísticas
        int totalResultados = resultados.size();
        Integer mejorPuntaje = resultados.stream()
                .mapToInt(ResultadoSaberPro::getPuntajeGlobal)
                .max()
                .orElse(0);
        
        Double promedioGlobal = resultados.stream()
                .mapToInt(ResultadoSaberPro::getPuntajeGlobal)
                .average()
                .orElse(0.0);
        
        // Calcular beneficios
        int totalBeneficios = 0;
        boolean tieneBeneficios = false;
        List<Map<String, Object>> beneficiosPrincipales = new ArrayList<>();
        
        for (ResultadoSaberPro resultado : resultados) {
            Beneficio beneficio;
            if ("SABER_TT".equals(resultado.getTipoPrueba())) {
                beneficio = beneficioService.calcularBeneficioSaberTyt(resultado.getPuntajeGlobal());
            } else {
                beneficio = beneficioService.calcularBeneficioSaberPro(resultado.getPuntajeGlobal());
            }
            
            resultado.setBeneficio(beneficio);
            
            if (beneficio.isAprobado() && (beneficio.getNotaAsignada() != null || beneficio.getPorcentajeBeca() > 0)) {
                totalBeneficios++;
                tieneBeneficios = true;
                
                // Agregar a beneficios principales (máximo 3)
                if (beneficiosPrincipales.size() < 3) {
                    Map<String, Object> beneficioMap = new HashMap<>();
                    beneficioMap.put("descripcion", beneficio.getDescripcion());
                    beneficioMap.put("notaAsignada", beneficio.getNotaAsignada());
                    beneficioMap.put("porcentajeBeca", beneficio.getPorcentajeBeca());
                    beneficiosPrincipales.add(beneficioMap);
                }
            }
        }
        
        // Resumen de competencias (del último resultado)
        List<Map<String, String>> competenciasResumen = new ArrayList<>();
        if (!resultados.isEmpty()) {
            ResultadoSaberPro ultimoResultado = resultados.get(0); // Último resultado
            
            if (ultimoResultado.getComunicacionEscrita() != null) {
                competenciasResumen.add(crearCompetenciaResumen("Comunicación", 
                    ultimoResultado.getComunicacionEscrita(), ultimoResultado.getNivelComunicacionEscrita()));
            }
            if (ultimoResultado.getRazonamientoCuantitativo() != null) {
                competenciasResumen.add(crearCompetenciaResumen("Razonamiento", 
                    ultimoResultado.getRazonamientoCuantitativo(), ultimoResultado.getNivelRazonamientoCuantitativo()));
            }
            if (ultimoResultado.getLecturaCritica() != null) {
                competenciasResumen.add(crearCompetenciaResumen("Lectura Crítica", 
                    ultimoResultado.getLecturaCritica(), ultimoResultado.getNivelLecturaCritica()));
            }
            if (ultimoResultado.getCompetenciasCiudadanas() != null) {
                competenciasResumen.add(crearCompetenciaResumen("Ciudadanas", 
                    ultimoResultado.getCompetenciasCiudadanas(), ultimoResultado.getNivelCompetenciasCiudadanas()));
            }
            if (ultimoResultado.getIngles() != null) {
                competenciasResumen.add(crearCompetenciaResumen("Inglés", 
                    ultimoResultado.getIngles(), ultimoResultado.getNivelIngles()));
            }
        }
        
        // Agregar datos al modelo
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("resultados", resultados);
        model.addAttribute("totalResultados", totalResultados);
        model.addAttribute("mejorPuntaje", mejorPuntaje);
        model.addAttribute("promedioGlobal", promedioGlobal);
        model.addAttribute("totalBeneficios", totalBeneficios);
        model.addAttribute("tieneBeneficios", tieneBeneficios);
        model.addAttribute("tieneResultados", !resultados.isEmpty());
        model.addAttribute("beneficiosPrincipales", beneficiosPrincipales);
        model.addAttribute("competenciasResumen", competenciasResumen);
        
        return "estudiante/dashboard";
    }


 // Método para identificación del estudiante
    @GetMapping("/identificacion")
    public String identificacion(HttpSession session, Model model) {
        if (!validarSesionEstudiante(session)) {
            return "redirect:/";
        }
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        // Obtener estadísticas del estudiante
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        int totalPruebas = resultados.size();
        
        Double promedioGlobal = resultados.stream()
                .mapToInt(ResultadoSaberPro::getPuntajeGlobal)
                .average()
                .orElse(0.0);
        
        String nivelActual = resultados.stream()
                .max(Comparator.comparing(ResultadoSaberPro::getFechaExamen))
                .map(ResultadoSaberPro::getNivelGlobal)
                .orElse("N/A");
        
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("totalPruebas", totalPruebas);
        model.addAttribute("promedioGlobal", promedioGlobal);
        model.addAttribute("nivelActual", nivelActual);
        
        return "estudiante/identificacion";
    }
    
 // Método para resultado único (mejorado)
    @GetMapping("/resultado-unico")
    public String resultadoUnico(@RequestParam(required = false) Long resultadoId, 
                                HttpSession session, 
                                Model model) {
        if (!validarSesionEstudiante(session)) {
            return "redirect:/";
        }
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        
        if (resultados.isEmpty()) {
            model.addAttribute("mensaje", "No se encontraron resultados para este estudiante.");
            model.addAttribute("estudiante", estudiante);
            return "estudiante/resultado-unico";
        }
        
        // Si hay múltiples resultados, mostrar selección
        if (resultados.size() > 1) {
            model.addAttribute("tieneMultiplesResultados", true);
            model.addAttribute("resultados", resultados);
            
            // Si se seleccionó un resultado específico
            if (resultadoId != null) {
                Optional<ResultadoSaberPro> resultadoSeleccionado = resultados.stream()
                        .filter(r -> r.getId().equals(resultadoId))
                        .findFirst();
                
                if (resultadoSeleccionado.isPresent()) {
                    model.addAttribute("resultado", resultadoSeleccionado.get());
                    // Calcular estadísticas adicionales
                    calcularEstadisticasResultado(model, resultadoSeleccionado.get());
                }
            }
        } else {
            // Solo un resultado
            ResultadoSaberPro resultado = resultados.get(0);
            model.addAttribute("resultado", resultado);
            model.addAttribute("tieneMultiplesResultados", false);
            // Calcular estadísticas adicionales
            calcularEstadisticasResultado(model, resultado);
        }
        
        model.addAttribute("estudiante", estudiante);
        return "estudiante/resultado-unico";
    }

 // Método para resultados totales del estudiante
    @GetMapping("/resultados-totales")
    public String resultadosTotales(HttpSession session, Model model) {
        if (!validarSesionEstudiante(session)) {
            return "redirect:/";
        }
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("resultados", resultados);
        model.addAttribute("totalResultados", resultados.size());
        
        return "estudiante/resultados-totales";
    }

 // Método para beneficios del estudiante
    @GetMapping("/beneficios")
    public String beneficios(HttpSession session, Model model) {
        if (!validarSesionEstudiante(session)) {
            return "redirect:/";
        }
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        
        // Calcular beneficios
        int totalBeneficios = 0;
        int becasObtenidas = 0;
        int exoneraciones = 0;
        
        for (ResultadoSaberPro resultado : resultados) {
            Beneficio beneficio;
            if ("SABER_TT".equals(resultado.getTipoPrueba())) {
                beneficio = beneficioService.calcularBeneficioSaberTyt(resultado.getPuntajeGlobal());
            } else {
                beneficio = beneficioService.calcularBeneficioSaberPro(resultado.getPuntajeGlobal());
            }
            
            resultado.setBeneficio(beneficio);
            
            if (beneficio.isAprobado()) {
                totalBeneficios++;
                if (beneficio.getPorcentajeBeca() > 0) {
                    becasObtenidas++;
                }
                if (beneficio.getNotaAsignada() != null) {
                    exoneraciones++;
                }
            }
        }
        
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("resultados", resultados);
        model.addAttribute("tieneResultados", !resultados.isEmpty());
        model.addAttribute("totalBeneficios", totalBeneficios);
        model.addAttribute("becasObtenidas", becasObtenidas);
        model.addAttribute("exoneraciones", exoneraciones);
        
        return "estudiante/beneficios";
    }

    @GetMapping("/comparativa")
    public String comparativa(HttpSession session, Model model) {
        if (!validarSesionEstudiante(session)) {
            return "redirect:/";
        }
        
        Estudiante estudiante = obtenerEstudianteDeSesion(session);
        if (estudiante == null) {
            return "redirect:/logout";
        }
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerResultadosPorEstudianteId(estudiante.getId());
        
        // Obtener promedios generales para comparación
        Double promedioPro = resultadoService.obtenerPromedioPorTipoPrueba("SABER_PRO");
        Double promedioTyT = resultadoService.obtenerPromedioPorTipoPrueba("SABER_TT");
        
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("resultados", resultados);
        model.addAttribute("promedioPro", promedioPro);
        model.addAttribute("promedioTyT", promedioTyT);
        
        return "estudiante/comparativa";
    }

    // 🛡️ MÉTODOS AUXILIARES

    private boolean validarSesionEstudiante(HttpSession session) {
        return session.getAttribute("rol") != null && 
               "ESTUDIANTE".equals(session.getAttribute("rol"));
    }

    private Estudiante obtenerEstudianteDeSesion(HttpSession session) {
        // En una implementación real, esto vendría de la relación Usuario-Estudiante
        // Por ahora, simulamos obteniendo el primer estudiante
        String username = (String) session.getAttribute("username");
        if (username != null) {
            // Buscar estudiante por número de documento (asumiendo que username es el documento)
            Optional<Estudiante> estudiante = estudianteService.buscarPorDocumento(username).stream().findFirst();
            return estudiante.orElse(null);
        }
        return null;
    }
    
    private Map<String, String> crearCompetenciaResumen(String nombre, Integer puntaje, String nivel) {
        Map<String, String> competencia = new HashMap<>();
        competencia.put("nombre", nombre);
        competencia.put("puntaje", puntaje != null ? puntaje.toString() : "N/A");
        competencia.put("nivel", nivel != null ? nivel : "N/A");
        return competencia;
    }
    
    private void calcularEstadisticasResultado(Model model, ResultadoSaberPro resultado) {
        // Simular percentil (en implementación real vendría de la base de datos)
        int percentil = 75; // Ejemplo: percentil 75
        model.addAttribute("posicionPercentil", percentil);
        
        // Comparativa con promedio nacional (ejemplo)
        String comparativa = resultado.getPuntajeGlobal() > 180 ? "+15 puntos" : "-5 puntos";
        model.addAttribute("comparativaPromedio", comparativa);
    }
    
 // Métodos auxiliares
    private String calcularNivelPromedio(List<ResultadoSaberPro> resultados) {
        if (resultados.isEmpty()) return "N/A";
        
        // Lógica simplificada para calcular nivel promedio
        double promedio = resultados.stream()
                .mapToInt(ResultadoSaberPro::getPuntajeGlobal)
                .average()
                .orElse(0.0);
        
        if (promedio >= 191) return "Nivel 4";
        else if (promedio >= 156) return "Nivel 3";
        else if (promedio >= 126) return "Nivel 2";
        else return "Nivel 1";
    }

    private List<Map<String, Object>> generarComparativaCompetencias(ResultadoSaberPro resultado) {
        List<Map<String, Object>> comparativas = new ArrayList<>();
        
        // Ejemplo de datos para comparativa (en implementación real vendrían de BD)
        if (resultado.getComunicacionEscrita() != null) {
            comparativas.add(crearComparativaCompetencia("Comunicación Escrita", 
                resultado.getComunicacionEscrita(), 150, 145));
        }
        if (resultado.getRazonamientoCuantitativo() != null) {
            comparativas.add(crearComparativaCompetencia("Razonamiento Cuantitativo", 
                resultado.getRazonamientoCuantitativo(), 155, 148));
        }
        if (resultado.getLecturaCritica() != null) {
            comparativas.add(crearComparativaCompetencia("Lectura Crítica", 
                resultado.getLecturaCritica(), 160, 152));
        }
        
        return comparativas;
    }

    private Map<String, Object> crearComparativaCompetencia(String nombre, Integer tuPuntaje, 
                                                           Integer promedioInstitucional, Integer promedioNacional) {
        Map<String, Object> comparativa = new HashMap<>();
        comparativa.put("nombre", nombre);
        comparativa.put("tuPuntaje", tuPuntaje);
        comparativa.put("promedioInstitucional", promedioInstitucional);
        comparativa.put("diferencia", tuPuntaje - promedioInstitucional);
        
        // Determinar estado
        if (tuPuntaje > promedioInstitucional + 10) {
            comparativa.put("estado", "SUPERIOR");
        } else if (tuPuntaje < promedioInstitucional - 10) {
            comparativa.put("estado", "INFERIOR");
        } else {
            comparativa.put("estado", "SIMILAR");
        }
        
        return comparativa;
    }

    private List<Map<String, String>> generarRecomendaciones(ResultadoSaberPro resultado) {
        List<Map<String, String>> recomendaciones = new ArrayList<>();
        
        // Recomendaciones basadas en el puntaje
        if (resultado.getPuntajeGlobal() < 180 && "SABER_PRO".equals(resultado.getTipoPrueba())) {
            recomendaciones.add(crearRecomendacion("📚", "Fortalecer competencias genéricas", 
                "Enfócate en mejorar lectura crítica y comunicación escrita para aumentar tu puntaje global."));
        }
        
        if (resultado.getIngles() != null && resultado.getIngles() < 150) {
            recomendaciones.add(crearRecomendacion("🌐", "Mejorar nivel de inglés", 
                "Practica regularmente reading comprehension y vocabulary para subir tu puntaje en inglés."));
        }
        
        recomendaciones.add(crearRecomendacion("🎯", "Práctica constante", 
            "Realiza simulacros periódicos para familiarizarte con el formato de la prueba."));
        
        return recomendaciones;
    }

    private Map<String, String> crearRecomendacion(String emoji, String titulo, String descripcion) {
        Map<String, String> recomendacion = new HashMap<>();
        recomendacion.put("emoji", emoji);
        recomendacion.put("titulo", titulo);
        recomendacion.put("descripcion", descripcion);
        return recomendacion;
    }
}

