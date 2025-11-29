package com.saberpro.sistema.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.saberpro.sistema.entity.EstadisticasPuntaje;
import com.saberpro.sistema.entity.Estudiante;
import com.saberpro.sistema.entity.ResultadoSaberPro;
import com.saberpro.sistema.entity.Usuario;
import com.saberpro.sistema.service.BeneficioService;
import com.saberpro.sistema.service.EstudianteService;
import com.saberpro.sistema.service.ResultadoSaberProService;
import com.saberpro.sistema.service.UsuarioService;


import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Controller
@RequestMapping("/coordinacion")
public class CoordinacionController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private ResultadoSaberProService resultadoService;
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BeneficioService beneficioService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        
        // Estadísticas para el dashboard
        long totalEstudiantes = estudianteService.contarTotalEstudiantes();
        long totalResultados = resultadoService.contarTotalResultados();
        Double promedioPro = resultadoService.obtenerPromedioPorTipoPrueba("SABER_PRO");
        Double promedioTyT = resultadoService.obtenerPromedioPorTipoPrueba("SABER_TT");
        
        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("totalResultados", totalResultados);
        model.addAttribute("promedioPro", promedioPro != null ? String.format("%.2f", promedioPro) : "N/A");
        model.addAttribute("promedioTyT", promedioTyT != null ? String.format("%.2f", promedioTyT) : "N/A");
        
        return "coordinacion/dashboard";
    }

    // 🔧 CRUD ESTUDIANTES

    @GetMapping("/estudiantes")
    public String listarEstudiantes(HttpSession session, Model model) {
        
        List<Estudiante> estudiantes = estudianteService.obtenerTodosEstudiantes();
        model.addAttribute("estudiantes", estudiantes);
        return "coordinacion/estudiantes";
    }

    @GetMapping("/estudiantes/nuevo")
    public String mostrarFormularioNuevoEstudiante(HttpSession session, Model model) {
        
        Estudiante estudiante = new Estudiante();
        // Establecer valores por defecto
        estudiante.setTipoDocumento("CC");
        model.addAttribute("estudiante", estudiante);
        return "coordinacion/estudiante-form";
    }

    @PostMapping("/estudiantes/guardar")
    public String guardarEstudiante(@ModelAttribute Estudiante estudiante,
    							   @RequestParam String password,	
                                   HttpSession session,
                                   Model model) {
        
        try {
            // Validar que el número de documento no exista (excepto en edición)
            if (estudiante.getId() == null) {
                Optional<Estudiante> existente = estudianteService.buscarPorDocumento(estudiante.getNumeroDocumento())
                        .stream().findFirst();
                if (existente.isPresent()) {
                    model.addAttribute("error", "Ya existe un estudiante con este número de documento");
                    return "coordinacion/estudiante-form";
                }
            }
            
            estudianteService.guardarEstudiante(estudiante);
            if (estudiante.getId() != null) {
                crearUsuarioParaEstudiante(estudiante, password);
            }
            return "redirect:/coordinacion/estudiantes?success=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el estudiante: " + e.getMessage());
            return "coordinacion/estudiante-form";
        }
    }

    @GetMapping("/estudiantes/editar/{id}")
    public String mostrarFormularioEditarEstudiante(@PathVariable Long id, HttpSession session, Model model) {
        
        Optional<Estudiante> estudiante = estudianteService.obtenerEstudiantePorId(id);
        if (estudiante.isPresent()) {
            model.addAttribute("estudiante", estudiante.get());
            return "coordinacion/estudiante-form";
        }
        return "redirect:/coordinacion/estudiantes";
    }

    @GetMapping("/estudiantes/eliminar/{id}")
    public String eliminarEstudiante(@PathVariable Long id, HttpSession session) {
        
        estudianteService.eliminarEstudiante(id);
        return "redirect:/coordinacion/estudiantes";
    }

    // 📊 GESTIÓN DE RESULTADOS

    @GetMapping("/resultados")
    public String listarResultados(HttpSession session, Model model) {
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerTodosResultados();
        
        // Calcular estadísticas
        long resultadosPro = resultados.stream()
                .filter(r -> "SABER_PRO".equals(r.getTipoPrueba()))
                .count();
        
        long resultadosTyT = resultados.stream()
                .filter(r -> "SABER_TT".equals(r.getTipoPrueba()))
                .count();
        
        Double promedioGlobal = resultados.stream()
                .mapToInt(ResultadoSaberPro::getPuntajeGlobal)
                .average()
                .orElse(0.0);
        
        model.addAttribute("resultados", resultados);
        model.addAttribute("resultadosPro", resultadosPro);
        model.addAttribute("resultadosTyT", resultadosTyT);
        model.addAttribute("promedioGlobal", promedioGlobal);
        
        return "coordinacion/resultados";
    }

    @GetMapping("/resultados/nuevo")
    public String mostrarFormularioNuevoResultado(HttpSession session, Model model) {
        
        List<Estudiante> estudiantes = estudianteService.obtenerTodosEstudiantes();
        ResultadoSaberPro resultado = new ResultadoSaberPro();
        resultado.setFechaExamen(LocalDate.now());
        
        model.addAttribute("resultado", resultado);
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("tiposPrueba", List.of("SABER_PRO", "SABER_TT"));
        
        return "coordinacion/resultado-form";
    }
    
 // Método para procesar guardado de resultado (mejorado)
    @PostMapping("/resultados/guardar")
    public String guardarResultado(@ModelAttribute ResultadoSaberPro resultado, 
                                  HttpSession session,
                                  Model model) {
        
        try {
            // Validar puntajes mínimos
            if ("SABER_TT".equals(resultado.getTipoPrueba()) && resultado.getPuntajeGlobal() < 80) {
                model.addAttribute("error", "Para Saber T&T el puntaje mínimo es 80 puntos");
                return cargarDatosFormulario(model);
            }
            
            if ("SABER_PRO".equals(resultado.getTipoPrueba()) && resultado.getPuntajeGlobal() < 120) {
                model.addAttribute("error", "Para Saber Pro el puntaje mínimo es 120 puntos");
                return cargarDatosFormulario(model);
            }
            
            // Calcular niveles automáticamente
            resultado.setNivelGlobal(calcularNivel(resultado.getPuntajeGlobal(), resultado.getTipoPrueba()));
            resultado.setNivelComunicacionEscrita(calcularNivelCompetencia(resultado.getComunicacionEscrita()));
            resultado.setNivelRazonamientoCuantitativo(calcularNivelCompetencia(resultado.getRazonamientoCuantitativo()));
            resultado.setNivelLecturaCritica(calcularNivelCompetencia(resultado.getLecturaCritica()));
            resultado.setNivelCompetenciasCiudadanas(calcularNivelCompetencia(resultado.getCompetenciasCiudadanas()));
            resultado.setNivelIngles(calcularNivelCompetencia(resultado.getIngles()));
            
            if (resultado.getFormulacionProyectos() != null) {
                resultado.setNivelFormulacionProyectos(calcularNivelCompetencia(resultado.getFormulacionProyectos()));
            }
            if (resultado.getPensamientoCientifico() != null) {
                resultado.setNivelPensamientoCientifico(calcularNivelCompetencia(resultado.getPensamientoCientifico()));
            }
            if (resultado.getDisenoSoftware() != null) {
                resultado.setNivelDisenoSoftware(calcularNivelCompetencia(resultado.getDisenoSoftware()));
            }
            
            resultadoService.guardarResultado(resultado);
            return "redirect:/coordinacion/resultados?success=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el resultado: " + e.getMessage());
            return cargarDatosFormulario(model);
        }
    }
    
 // Método para mostrar formulario de edición
    @GetMapping("/resultados/editar/{id}")
    public String mostrarFormularioEditarResultado(@PathVariable Long id, HttpSession session, Model model) {
        
        Optional<ResultadoSaberPro> resultadoOpt = resultadoService.obtenerResultadoPorId(id);
        if (resultadoOpt.isPresent()) {
            List<Estudiante> estudiantes = estudianteService.obtenerTodosEstudiantes();
            
            model.addAttribute("resultado", resultadoOpt.get());
            model.addAttribute("estudiantes", estudiantes);
            model.addAttribute("tiposPrueba", List.of("SABER_PRO", "SABER_TT"));
            
            return "coordinacion/resultado-form";
        }
        
        return "redirect:/coordinacion/resultados";
    }
    
 // Método para ver detalle de resultado
    @GetMapping("/resultados/detalle/{id}")
    public String verDetalleResultado(@PathVariable Long id, HttpSession session, Model model) {
        
        Optional<ResultadoSaberPro> resultadoOpt = resultadoService.obtenerResultadoPorId(id);
        if (resultadoOpt.isPresent()) {
            ResultadoSaberPro resultado = resultadoOpt.get();
            
            // Calcular beneficio
            if ("SABER_TT".equals(resultado.getTipoPrueba())) {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberTyt(resultado.getPuntajeGlobal()));
            } else {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberPro(resultado.getPuntajeGlobal()));
            }
            
            model.addAttribute("resultado", resultado);
            return "coordinacion/resultado-detalle";
        }
        
        return "redirect:/coordinacion/resultados";
    }
    
 // Método para eliminar resultado
    @GetMapping("/resultados/eliminar/{id}")
    public String eliminarResultado(@PathVariable Long id, HttpSession session) {
        
        resultadoService.eliminarResultado(id);
        return "redirect:/coordinacion/resultados?deleted=true";
    }

    // 📈 INFORMES

    @GetMapping("/informes/alumnos")
    public String informeAlumnos(HttpSession session, Model model) {
        
        List<Estudiante> estudiantes = estudianteService.obtenerTodosEstudiantes();
        model.addAttribute("estudiantes", estudiantes);
        return "coordinacion/informe-alumnos";
    }

    @GetMapping("/informes/detallado")
    public String informeDetallado(HttpSession session, Model model) {
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerTodosResultados();
        model.addAttribute("resultados", resultados);
        return "coordinacion/informe-detallado";
    }

 // Método para el informe de beneficios (completo con estadísticas)
    @GetMapping("/informes/beneficios")
    public String informeBeneficios(HttpSession session, Model model) {
        
        List<ResultadoSaberPro> resultados = resultadoService.obtenerTodosResultados();
        
        // Calcular beneficios para cada resultado
        resultados.forEach(resultado -> {
            if ("SABER_TT".equals(resultado.getTipoPrueba())) {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberTyt(resultado.getPuntajeGlobal()));
            } else {
                resultado.setBeneficio(beneficioService.calcularBeneficioSaberPro(resultado.getPuntajeGlobal()));
            }
        });
        
        // Calcular estadísticas
        long totalEstudiantes = estudianteService.contarTotalEstudiantes();
        long estudiantesConResultados = resultados.stream().map(r -> r.getEstudiante().getId()).distinct().count();
        
        // Estadísticas Saber Pro
        long beneficiosProExcelente = resultados.stream()
                .filter(r -> "SABER_PRO".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() > 241)
                .count();
        
        long beneficiosProMuyBueno = resultados.stream()
                .filter(r -> "SABER_PRO".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 211 && r.getPuntajeGlobal() <= 240)
                .count();
        
        long beneficiosProBueno = resultados.stream()
                .filter(r -> "SABER_PRO".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 180 && r.getPuntajeGlobal() <= 210)
                .count();
        
        long aprobadosPro = resultados.stream()
                .filter(r -> "SABER_PRO".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 120)
                .count();
        
        // Estadísticas Saber T&T
        long beneficiosTyTExcelente = resultados.stream()
                .filter(r -> "SABER_TT".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() > 171)
                .count();
        
        long beneficiosTyTMuyBueno = resultados.stream()
                .filter(r -> "SABER_TT".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 151 && r.getPuntajeGlobal() <= 170)
                .count();
        
        long beneficiosTyTBueno = resultados.stream()
                .filter(r -> "SABER_TT".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 120 && r.getPuntajeGlobal() <= 150)
                .count();
        
        long aprobadosTyT = resultados.stream()
                .filter(r -> "SABER_TT".equals(r.getTipoPrueba()) && r.getPuntajeGlobal() >= 80)
                .count();
        
        // Impacto económico
        long becas100 = resultados.stream()
                .filter(r -> r.getBeneficio() != null && r.getBeneficio().getPorcentajeBeca() == 100.0)
                .count();
        
        long becas50 = resultados.stream()
                .filter(r -> r.getBeneficio() != null && r.getBeneficio().getPorcentajeBeca() == 50.0)
                .count();
        
        long totalExonerados = resultados.stream()
                .filter(r -> r.getBeneficio() != null && r.getBeneficio().getNotaAsignada() != null)
                .count();
        
        long estudiantesConBeneficios = resultados.stream()
                .filter(r -> r.getBeneficio() != null && 
                            (r.getBeneficio().getNotaAsignada() != null || r.getBeneficio().getPorcentajeBeca() > 0))
                .count();
        
        long estudiantesAprobados = resultados.stream()
                .filter(r -> r.getBeneficio() != null && r.getBeneficio().isAprobado())
                .count();
        
        double porcentajeBeneficios = estudiantesConResultados > 0 ? 
                (estudiantesConBeneficios * 100.0) / estudiantesConResultados : 0.0;
        
        // Agregar datos al modelo
        model.addAttribute("resultados", resultados);
        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("estudiantesConBeneficios", estudiantesConBeneficios);
        model.addAttribute("estudiantesAprobados", estudiantesAprobados);
        model.addAttribute("porcentajeBeneficios", String.format("%.1f", porcentajeBeneficios));
        
        model.addAttribute("beneficiosProExcelente", beneficiosProExcelente);
        model.addAttribute("beneficiosProMuyBueno", beneficiosProMuyBueno);
        model.addAttribute("beneficiosProBueno", beneficiosProBueno);
        model.addAttribute("aprobadosPro", aprobadosPro);
        
        model.addAttribute("beneficiosTyTExcelente", beneficiosTyTExcelente);
        model.addAttribute("beneficiosTyTMuyBueno", beneficiosTyTMuyBueno);
        model.addAttribute("beneficiosTyTBueno", beneficiosTyTBueno);
        model.addAttribute("aprobadosTyT", aprobadosTyT);
        
        model.addAttribute("becas100", becas100);
        model.addAttribute("becas50", becas50);
        model.addAttribute("totalExonerados", totalExonerados);
        
        return "coordinacion/informe-beneficios";
    }

    @GetMapping("/informes/estadisticas")
public String informeEstadisticas(HttpSession session, Model model) {
        
        String tipoPro = "SABER_PRO";
        String tipoTyt = "SABER_TT";

        // ==========================================================
        // 1. OBTENER Y CALCULAR ESTADÍSTICAS PARA SABER PRO
        // ==========================================================
        EstadisticasPuntaje estadisticasPro = new EstadisticasPuntaje();
        
        Long totalPro = resultadoService.contarResultadosPorTipoPrueba(tipoPro); // **(Método nuevo requerido)**
        Double promedioPro = resultadoService.obtenerPromedioPorTipoPrueba(tipoPro);
        Long aprobadosPro = resultadoService.contarAprobadosPorTipoPrueba(tipoPro); // **(Método nuevo requerido)**
        Long conBeneficiosPro = resultadoService.contarConBeneficiosPorTipoPrueba(tipoPro); // **(Método nuevo requerido)**
        
        // Cálculos de porcentajes
        Double tasaAprobacionPro = totalPro != null && totalPro > 0 ? (aprobadosPro * 100.0) / totalPro : 0.0;
        Double porcentajeBeneficiosPro = totalPro != null && totalPro > 0 ? (conBeneficiosPro * 100.0) / totalPro : 0.0;

        // Llenar el DTO de Pro
        estadisticasPro.setTotal(totalPro);
        estadisticasPro.setPromedio(promedioPro);
        estadisticasPro.setAprobados(aprobadosPro);
        estadisticasPro.setTasaAprobacion(tasaAprobacionPro);
        estadisticasPro.setConBeneficios(conBeneficiosPro);
        estadisticasPro.setPorcentajeBeneficios(porcentajeBeneficiosPro);

        // AÑADIR DTO DE PRO
        model.addAttribute("estadisticasPro", estadisticasPro);
        resultadoService.calcularDistribucionPorNivel(tipoPro, estadisticasPro);


        // ==========================================================
        // 2. OBTENER Y CALCULAR ESTADÍSTICAS PARA SABER T&T (Si es necesario)
        // ==========================================================
        EstadisticasPuntaje estadisticasTyT = new EstadisticasPuntaje();
        
        Long totalTyT = resultadoService.contarResultadosPorTipoPrueba(tipoTyt);
        Double promedioTyT = resultadoService.obtenerPromedioPorTipoPrueba(tipoTyt);
        Long aprobadosTyT = resultadoService.contarAprobadosPorTipoPrueba(tipoTyt);
        Long conBeneficiosTyT = resultadoService.contarConBeneficiosPorTipoPrueba(tipoTyt); 

        Double tasaAprobacionTyT = totalTyT != null && totalTyT > 0 ? (aprobadosTyT * 100.0) / totalTyT : 0.0;
        Double porcentajeBeneficiosTyT = totalTyT != null && totalTyT > 0 ? (conBeneficiosTyT * 100.0) / totalTyT : 0.0;
        
        estadisticasTyT.setTotal(totalTyT);
        estadisticasTyT.setPromedio(promedioTyT);
        estadisticasTyT.setAprobados(aprobadosTyT);
        estadisticasTyT.setTasaAprobacion(tasaAprobacionTyT);
        estadisticasTyT.setConBeneficios(conBeneficiosTyT);
        estadisticasTyT.setPorcentajeBeneficios(porcentajeBeneficiosTyT);

        // AÑADIR DTO DE T&T
        model.addAttribute("estadisticasTyT", estadisticasTyT);
        resultadoService.calcularDistribucionPorNivel(tipoTyt, estadisticasTyT);
        

        // ==========================================================
        // 3. Variables generales
        // ==========================================================
        Long totalEstudiantes = estudianteService.contarTotalEstudiantes();
        Long estudiantesConResultado = resultadoService.contarEstudiantesConResultado();
        List<Object[]> topResultados = resultadoService.obtenerTop10Resultados(); // Asegúrate de que esto sea el Top 10 general o de PRO

        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("estudiantesConResultado", estudiantesConResultado);
        // model.addAttribute("promedioPro", promedioPro); // Ya está en estadisticasPro
        model.addAttribute("promedioTyT", promedioTyT); 
        model.addAttribute("topResultados", topResultados);
        
        return "coordinacion/informe-estadisticas";
    }
    
    

    // 🔍 BÚSQUEDAS

    @GetMapping("/buscar")
    public String buscarEstudiantes(@RequestParam String criterio, 
                                   @RequestParam String valor, 
                                   HttpSession session, 
                                   Model model) {
        
        List<Estudiante> estudiantes;
        
        switch (criterio) {
            case "documento":
                estudiantes = estudianteService.buscarPorDocumento(valor);
                break;
            case "apellido":
                estudiantes = estudianteService.buscarPorApellido(valor);
                break;
            case "nombre":
                estudiantes = estudianteService.buscarPorNombre(valor);
                break;
            case "registro":
                estudiantes = estudianteService.buscarPorRegistro(valor);
                break;
            default:
                estudiantes = estudianteService.obtenerTodosEstudiantes();
        }
        
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("criterio", criterio);
        model.addAttribute("valor", valor);
        
        return "coordinacion/estudiantes";
    }

    // 🛡️ MÉTODOS AUXILIARES

    private String calcularNivel(Integer puntaje, String tipoPrueba) {
        if (puntaje == null) return "N/A";
        
        if ("SABER_TT".equals(tipoPrueba)) {
            if (puntaje >= 191) return "Nivel 4";
            else if (puntaje >= 156) return "Nivel 3";
            else if (puntaje >= 126) return "Nivel 2";
            else return "Nivel 1";
        } else {
            // Para competencias individuales
            if (puntaje >= 191) return "Nivel 4";
            else if (puntaje >= 156) return "Nivel 3";
            else if (puntaje >= 126) return "Nivel 2";
            else return "Nivel 1";
        }
    }
    
 // Método auxiliar para cargar datos del formulario
    private String cargarDatosFormulario(Model model) {
        List<Estudiante> estudiantes = estudianteService.obtenerTodosEstudiantes();
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("tiposPrueba", List.of("SABER_PRO", "SABER_TT"));
        return "coordinacion/resultado-form";
    }

    // Método para calcular nivel de competencia
    private String calcularNivelCompetencia(Integer puntaje) {
        if (puntaje == null) return null;
        return calcularNivel(puntaje, "COMPETENCIA");
    }
    
    private void crearUsuarioParaEstudiante(Estudiante estudiante, String password) {
        try {
            // Verificar si ya existe un usuario para este estudiante
            if (!usuarioService.existeUsername(estudiante.getNumeroDocumento())) {
                Usuario usuario = new Usuario();
                usuario.setUsername(estudiante.getNumeroDocumento()); // Usar documento como usuario
                usuario.setPassword(password); // Password por defecto
                usuario.setRol("ESTUDIANTE");
                usuario.setNombre(estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido());
                usuario.setEmail(estudiante.getCorreoElectronico());
                
                usuarioService.guardarUsuario(usuario);
                System.out.println("✅ Usuario creado para estudiante: " + estudiante.getNumeroDocumento());
            }
        } catch (Exception e) {
            System.err.println("❌ Error creando usuario para estudiante: " + e.getMessage());
        }
    }
}
