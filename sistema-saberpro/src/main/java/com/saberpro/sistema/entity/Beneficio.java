package com.saberpro.sistema.entity;


import lombok.Data;

@Data
public class Beneficio {
    private String descripcion;
    private Double notaAsignada;
    private Double porcentajeBeca;
    private boolean aprobado;
    private String mensaje;
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Double getNotaAsignada() {
		return notaAsignada;
	}
	public void setNotaAsignada(Double notaAsignada) {
		this.notaAsignada = notaAsignada;
	}
	public Double getPorcentajeBeca() {
		return porcentajeBeca;
	}
	public void setPorcentajeBeca(Double porcentajeBeca) {
		this.porcentajeBeca = porcentajeBeca;
	}
	public boolean isAprobado() {
		return aprobado;
	}
	public void setAprobado(boolean aprobado) {
		this.aprobado = aprobado;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
    
    
}