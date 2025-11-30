package com.messismo.bar.Services;

import com.messismo.bar.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClientIdService {

    private final UserRepository userRepository;
    private final Random random = new Random();

    public String generateUniqueClientId() {
        Set<String> existingIds = userRepository.findAllClientIds();
        
        // Empezar con 4 dígitos
        int digits = 4;
        int maxAttempts = 100; // Evitar bucle infinito
        
        while (digits <= 10) { // Límite razonable
            int minValue = (int) Math.pow(10, digits - 1);
            int maxValue = (int) Math.pow(10, digits) - 1;
            
            // Si ya no hay números disponibles en esta cantidad de dígitos
            if (existingIds.size() >= (maxValue - minValue + 1)) {
                digits++;
                continue;
            }
            
            String newId;
            int attempts = 0;
            
            do {
                int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
                newId = String.valueOf(randomNumber);
                attempts++;
                
                if (attempts > maxAttempts) {
                    // Si no encuentra un ID libre, pasar al siguiente nivel de dígitos
                    break;
                }
            } while (existingIds.contains(newId));
            
            if (attempts <= maxAttempts && !existingIds.contains(newId)) {
                return newId;
            }
            
            digits++;
        }
        
        throw new RuntimeException("Unable to generate unique client ID");
    }
}