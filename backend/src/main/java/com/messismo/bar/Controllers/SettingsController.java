package com.messismo.bar.Controllers;

import com.messismo.bar.Entities.Settings;
import com.messismo.bar.Services.SettingsService;
import com.messismo.bar.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserService userService;

    // Obtener todas las configuraciones (solo ADMIN/MANAGER)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Settings>> getAllSettings() {
        List<Settings> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    // Obtener la tasa de conversión de puntos (cualquier usuario autenticado puede verla)
    @GetMapping("/points-conversion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE') or hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getPointsConversionRate() {
        double rate = settingsService.getPointsConversionRate();
        return ResponseEntity.ok(Map.of(
            "conversionRate", rate,
            "description", "Cantidad en pesos necesaria para obtener 1 punto"
        ));
    }

    // Actualizar la tasa de conversión de puntos (solo ADMIN/MANAGER)
    @PutMapping("/points-conversion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Settings> updatePointsConversionRate(@RequestBody Map<String, Object> request) {
        try {
            Double rate = Double.valueOf(request.get("conversionRate").toString());
            
            if (rate <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            Settings updatedSetting = settingsService.updatePointsConversionRate(rate);
            return ResponseEntity.ok(updatedSetting);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener una configuración específica por clave (solo ADMIN/MANAGER)
    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Settings> getSettingByKey(@PathVariable String key) {
        Settings setting = settingsService.getSettingByKey(key);
        if (setting != null) {
            return ResponseEntity.ok(setting);
        }
        return ResponseEntity.notFound().build();
    }

    // Actualizar o crear una configuración (solo ADMIN/MANAGER)
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Settings> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        
        String value = request.get("value");
        String description = request.get("description");
        
        if (value == null || value.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Settings savedSetting = settingsService.saveSetting(key, value, description);
        return ResponseEntity.ok(savedSetting);
    }

    // Obtener lista de clientes (accesible para VALIDATEDEMPLOYEE)
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE')")
    public ResponseEntity<?> getClients() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllClients());
    }
    
    // Obtener historial de cambios de una configuración (solo ADMIN/MANAGER)
    @GetMapping("/{key}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<com.messismo.bar.Entities.SettingsHistory>> getSettingsHistory(@PathVariable String key) {
        List<com.messismo.bar.Entities.SettingsHistory> history = settingsService.getSettingsHistory(key);
        return ResponseEntity.ok(history);
    }
}