package com.saberpro.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saberpro.sistema.entity.Estudiante;
import com.saberpro.sistema.repository.EstudianteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EstudianteService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteRepository.findAll();
    }

    public Optional<Estudiante> obtenerEstudiantePorId(Long id) {
        return estudianteRepository.findById(id);
    }

    public Estudiante guardarEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    public void eliminarEstudiante(Long id) {
        estudianteRepository.deleteById(id);
    }

    public List<Estudiante> buscarPorDocumento(String documento) {
        return estudianteRepository.findByNumeroDocumento(documento)
                .map(List::of)
                .orElse(List.of());
    }

    public List<Estudiante> buscarPorApellido(String apellido) {
        return estudianteRepository.findByPrimerApellidoContainingIgnoreCase(apellido);
    }

    public List<Estudiante> buscarPorNombre(String nombre) {
        return estudianteRepository.findByNombreContaining(nombre);
    }

    public List<Estudiante> buscarPorRegistro(String registro) {
        return estudianteRepository.findByNumeroRegistro(registro)
                .map(List::of)
                .orElse(List.of());
    }

    public long contarTotalEstudiantes() {
        return estudianteRepository.count();
    }
}
