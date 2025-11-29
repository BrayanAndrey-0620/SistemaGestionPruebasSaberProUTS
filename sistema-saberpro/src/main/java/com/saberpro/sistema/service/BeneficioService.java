package com.saberpro.sistema.service;


import org.springframework.stereotype.Service;

import com.saberpro.sistema.entity.Beneficio;

@Service
public class BeneficioService {
    
    public Beneficio calcularBeneficioSaberTyt(Integer puntaje) {
        Beneficio beneficio = new Beneficio();
        beneficio.setAprobado(puntaje >= 80);
        
        if (!beneficio.isAprobado()) {
            beneficio.setMensaje("No aprobado. Puntaje mínimo requerido: 80 puntos");
            return beneficio;
        }
        
        if (puntaje >= 120 && puntaje <= 150) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado II");
            beneficio.setNotaAsignada(4.5);
            beneficio.setPorcentajeBeca(0.0);
        } else if (puntaje >= 151 && puntaje <= 170) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado II");
            beneficio.setNotaAsignada(4.7);
            beneficio.setPorcentajeBeca(50.0);
        } else if (puntaje > 171) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado II");
            beneficio.setNotaAsignada(5.0);
            beneficio.setPorcentajeBeca(100.0);
        } else {
            beneficio.setDescripcion("Aprobado pero sin beneficios adicionales");
            beneficio.setNotaAsignada(null);
            beneficio.setPorcentajeBeca(0.0);
        }
        
        return beneficio;
    }
    
    public Beneficio calcularBeneficioSaberPro(Integer puntaje) {
        Beneficio beneficio = new Beneficio();
        beneficio.setAprobado(puntaje >= 120);
        
        if (!beneficio.isAprobado()) {
            beneficio.setMensaje("No aprobado. Puntaje mínimo requerido: 120 puntos");
            return beneficio;
        }
        
        if (puntaje >= 180 && puntaje <= 210) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado IV");
            beneficio.setNotaAsignada(4.5);
            beneficio.setPorcentajeBeca(0.0);
        } else if (puntaje >= 211 && puntaje <= 240) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado IV");
            beneficio.setNotaAsignada(4.7);
            beneficio.setPorcentajeBeca(50.0);
        } else if (puntaje > 241) {
            beneficio.setDescripcion("Exoneración de entrega de informe final de trabajo de grado o Seminario de grado IV");
            beneficio.setNotaAsignada(5.0);
            beneficio.setPorcentajeBeca(100.0);
        } else {
            beneficio.setDescripcion("Aprobado pero sin beneficios adicionales");
            beneficio.setNotaAsignada(null);
            beneficio.setPorcentajeBeca(0.0);
        }
        
        return beneficio;
    }
}