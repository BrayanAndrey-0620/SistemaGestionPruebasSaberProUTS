package com.saberpro.sistema;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna login.html
    }
    
    /*// ¡Aquí estaba el mapeo ambiguo! (Borrado)
    @GetMapping("/estudiante/dashboard")
    public String estudianteDashboard() {
        return "estudiante/dashboard"; // Asegúrate de que esta vista exista en templates/estudiante/dashboard.html
    }*/

    
    // Página de inicio alternativa
    //@GetMapping("/")
    //public String home() {
      //  return "redirect:/dashboard";
    //}
}