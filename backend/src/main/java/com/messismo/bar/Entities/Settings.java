package com.messismo.bar.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "settings")
@Data
public class Settings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "setting_key", unique = true, nullable = false)
    private String key;
    
    @Column(name = "setting_value", nullable = false)
    private String value;
    
    @Column(name = "description")
    private String description;
    
    public Settings() {}
    
    public Settings(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }
}