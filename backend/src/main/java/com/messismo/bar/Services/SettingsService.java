package com.messismo.bar.Services;

import com.messismo.bar.Entities.Settings;
import com.messismo.bar.Entities.SettingsHistory;
import com.messismo.bar.Repositories.SettingsRepository;
import com.messismo.bar.Repositories.SettingsHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;
    
    @Autowired
    private SettingsHistoryRepository settingsHistoryRepository;

    // Constante para la clave de conversión de puntos
    public static final String POINTS_CONVERSION_KEY = "points_conversion_rate";
    public static final String DEFAULT_POINTS_CONVERSION = "100"; // $100 = 1 punto por defecto

    @PostConstruct
    public void initializeDefaultSettings() {
        // Crear configuración por defecto si no existe
        if (settingsRepository.findByKey(POINTS_CONVERSION_KEY).isEmpty()) {
            Settings defaultSetting = new Settings(
                POINTS_CONVERSION_KEY, 
                DEFAULT_POINTS_CONVERSION,
                "Cantidad en pesos necesaria para obtener 1 punto de fidelidad"
            );
            settingsRepository.save(defaultSetting);
        }
    }

    public List<Settings> getAllSettings() {
        return settingsRepository.findAll();
    }

    public Settings getSettingByKey(String key) {
        return settingsRepository.findByKey(key).orElse(null);
    }

    public Settings saveSetting(String key, String value, String description) {
        Settings existingSetting = settingsRepository.findByKey(key).orElse(null);
        String oldValue = existingSetting != null ? existingSetting.getValue() : null;
        
        Settings setting = existingSetting != null ? existingSetting : new Settings(key, value, description);
        
        // Solo guardar historial si el valor cambió
        if (existingSetting != null && !value.equals(oldValue)) {
            SettingsHistory history = new SettingsHistory(
                key, 
                oldValue, 
                value, 
                "ADMIN", // TODO: obtener usuario actual del contexto de seguridad
                description != null ? description : setting.getDescription()
            );
            settingsHistoryRepository.save(history);
        }
        
        setting.setValue(value);
        if (description != null) {
            setting.setDescription(description);
        }
        
        return settingsRepository.save(setting);
    }

    // Método específico para obtener la tasa de conversión de puntos
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public double getPointsConversionRate() {
        // Forzar lectura fresca desde la base de datos
        Settings setting = settingsRepository.findByKey(POINTS_CONVERSION_KEY).orElse(null);
        
        if (setting != null) {
            try {
                double rate = Double.parseDouble(setting.getValue());
                return rate;
            } catch (NumberFormatException e) {
                // Si hay error, usar valor por defecto
                return Double.parseDouble(DEFAULT_POINTS_CONVERSION);
            }
        }
        return Double.parseDouble(DEFAULT_POINTS_CONVERSION);
    }

    // Método para actualizar la tasa de conversión de puntos
    public Settings updatePointsConversionRate(double rate) {
        return saveSetting(
            POINTS_CONVERSION_KEY, 
            String.valueOf(rate),
            "Cantidad en pesos necesaria para obtener 1 punto de fidelidad"
        );
    }
    
    // Método para obtener el historial de un setting
    public List<SettingsHistory> getSettingsHistory(String key) {
        return settingsHistoryRepository.findByKeyOrderByChangedAtDesc(key);
    }
}