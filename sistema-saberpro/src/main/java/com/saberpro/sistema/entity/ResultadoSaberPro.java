package com.saberpro.sistema.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "resultados_saber_pro")
@Data
public class ResultadoSaberPro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;
    
    @Column(nullable = false)
    private Integer puntajeGlobal;
    
    private String nivelGlobal;
    private Integer comunicacionEscrita;
    private String nivelComunicacionEscrita;
    private Integer razonamientoCuantitativo;
    private String nivelRazonamientoCuantitativo;
    private Integer lecturaCritica;
    private String nivelLecturaCritica;
    private Integer competenciasCiudadanas;
    private String nivelCompetenciasCiudadanas;
    private Integer ingles;
    private String nivelIngles;
    private Integer formulacionProyectos;
    private String nivelFormulacionProyectos;
    private Integer pensamientoCientifico;
    private String nivelPensamientoCientifico;
    private Integer disenoSoftware;
    private String nivelDisenoSoftware;
    private String nivelInglesMarco;
    
    private LocalDate fechaExamen;
    private String tipoPrueba; // SABER_PRO, SABER_TT
    
    @Transient
    private Beneficio beneficio;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Estudiante getEstudiante() {
		return estudiante;
	}

	public void setEstudiante(Estudiante estudiante) {
		this.estudiante = estudiante;
	}

	public Integer getPuntajeGlobal() {
		return puntajeGlobal;
	}

	public void setPuntajeGlobal(Integer puntajeGlobal) {
		this.puntajeGlobal = puntajeGlobal;
	}

	public String getNivelGlobal() {
		return nivelGlobal;
	}

	public void setNivelGlobal(String nivelGlobal) {
		this.nivelGlobal = nivelGlobal;
	}

	public Integer getComunicacionEscrita() {
		return comunicacionEscrita;
	}

	public void setComunicacionEscrita(Integer comunicacionEscrita) {
		this.comunicacionEscrita = comunicacionEscrita;
	}

	public String getNivelComunicacionEscrita() {
		return nivelComunicacionEscrita;
	}

	public void setNivelComunicacionEscrita(String nivelComunicacionEscrita) {
		this.nivelComunicacionEscrita = nivelComunicacionEscrita;
	}

	public Integer getRazonamientoCuantitativo() {
		return razonamientoCuantitativo;
	}

	public void setRazonamientoCuantitativo(Integer razonamientoCuantitativo) {
		this.razonamientoCuantitativo = razonamientoCuantitativo;
	}

	public String getNivelRazonamientoCuantitativo() {
		return nivelRazonamientoCuantitativo;
	}

	public void setNivelRazonamientoCuantitativo(String nivelRazonamientoCuantitativo) {
		this.nivelRazonamientoCuantitativo = nivelRazonamientoCuantitativo;
	}

	public Integer getLecturaCritica() {
		return lecturaCritica;
	}

	public void setLecturaCritica(Integer lecturaCritica) {
		this.lecturaCritica = lecturaCritica;
	}

	public String getNivelLecturaCritica() {
		return nivelLecturaCritica;
	}

	public void setNivelLecturaCritica(String nivelLecturaCritica) {
		this.nivelLecturaCritica = nivelLecturaCritica;
	}

	public Integer getCompetenciasCiudadanas() {
		return competenciasCiudadanas;
	}

	public void setCompetenciasCiudadanas(Integer competenciasCiudadanas) {
		this.competenciasCiudadanas = competenciasCiudadanas;
	}

	public String getNivelCompetenciasCiudadanas() {
		return nivelCompetenciasCiudadanas;
	}

	public void setNivelCompetenciasCiudadanas(String nivelCompetenciasCiudadanas) {
		this.nivelCompetenciasCiudadanas = nivelCompetenciasCiudadanas;
	}

	public Integer getIngles() {
		return ingles;
	}

	public void setIngles(Integer ingles) {
		this.ingles = ingles;
	}

	public String getNivelIngles() {
		return nivelIngles;
	}

	public void setNivelIngles(String nivelIngles) {
		this.nivelIngles = nivelIngles;
	}

	public Integer getFormulacionProyectos() {
		return formulacionProyectos;
	}

	public void setFormulacionProyectos(Integer formulacionProyectos) {
		this.formulacionProyectos = formulacionProyectos;
	}

	public String getNivelFormulacionProyectos() {
		return nivelFormulacionProyectos;
	}

	public void setNivelFormulacionProyectos(String nivelFormulacionProyectos) {
		this.nivelFormulacionProyectos = nivelFormulacionProyectos;
	}

	public Integer getPensamientoCientifico() {
		return pensamientoCientifico;
	}

	public void setPensamientoCientifico(Integer pensamientoCientifico) {
		this.pensamientoCientifico = pensamientoCientifico;
	}

	public String getNivelPensamientoCientifico() {
		return nivelPensamientoCientifico;
	}

	public void setNivelPensamientoCientifico(String nivelPensamientoCientifico) {
		this.nivelPensamientoCientifico = nivelPensamientoCientifico;
	}

	public Integer getDisenoSoftware() {
		return disenoSoftware;
	}

	public void setDisenoSoftware(Integer disenoSoftware) {
		this.disenoSoftware = disenoSoftware;
	}

	public String getNivelDisenoSoftware() {
		return nivelDisenoSoftware;
	}

	public void setNivelDisenoSoftware(String nivelDisenoSoftware) {
		this.nivelDisenoSoftware = nivelDisenoSoftware;
	}

	public String getNivelInglesMarco() {
		return nivelInglesMarco;
	}

	public void setNivelInglesMarco(String nivelInglesMarco) {
		this.nivelInglesMarco = nivelInglesMarco;
	}

	public LocalDate getFechaExamen() {
		return fechaExamen;
	}

	public void setFechaExamen(LocalDate fechaExamen) {
		this.fechaExamen = fechaExamen;
	}

	public String getTipoPrueba() {
		return tipoPrueba;
	}

	public void setTipoPrueba(String tipoPrueba) {
		this.tipoPrueba = tipoPrueba;
	}

	public Beneficio getBeneficio() {
		return beneficio;
	}

	public void setBeneficio(Beneficio beneficio) {
		this.beneficio = beneficio;
	}
    
}
