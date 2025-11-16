package com.messismo.bar.Services;

import com.messismo.bar.Entities.PointsAccount;
import com.messismo.bar.Entities.PointsTransaction;
import com.messismo.bar.Entities.TransactionType;
import com.messismo.bar.Repositories.PointsAccountRepository;
import com.messismo.bar.Repositories.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointsService {

    private final PointsAccountRepository pointsAccountRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final SettingsService settingsService;

    /**
     * Crea una nueva cuenta de puntos para un cliente
     */
    public PointsAccount createPointsAccount(String clientId) {
        if (pointsAccountRepository.existsByClientId(clientId)) {
            throw new RuntimeException("Points account already exists for client: " + clientId);
        }
        
        PointsAccount account = new PointsAccount(clientId);
        return pointsAccountRepository.save(account);
    }

    /**
     * Obtiene la cuenta de puntos de un cliente
     */
    public Optional<PointsAccount> getPointsAccount(String clientId) {
        return pointsAccountRepository.findByClientId(clientId);
    }

    /**
     * Añade puntos a la cuenta de un cliente por una orden cerrada
     */
    public Double addPointsForOrder(String clientId, Double orderTotal, Long orderId) {
        // Obtener la tasa de conversión dinámica desde configuración
        double conversionRate = settingsService.getPointsConversionRate();
        Double points = orderTotal / conversionRate;
        
        PointsAccount account = getOrCreateAccount(clientId);
        account.addPoints(points);
        pointsAccountRepository.save(account);
        
        // Registrar la transacción
        PointsTransaction transaction = new PointsTransaction(
            clientId, 
            TransactionType.EARNED, 
            points,
            "ORDER_#" + orderId,
            "Puntos ganados por orden de $" + String.format("%.2f", orderTotal) + 
            " (tasa: $" + String.format("%.0f", conversionRate) + " = 1 punto)"
        );
        pointsTransactionRepository.save(transaction);
        
        // Devolver los puntos calculados para que se guarden en la orden
        return points;
    }

    /**
     * Gasta puntos de la cuenta de un cliente
     */
    public boolean spendPoints(String clientId, Double points, String source, String description) {
        PointsAccount account = getOrCreateAccount(clientId);
        
        if (account.spendPoints(points)) {
            pointsAccountRepository.save(account);
            
            // Registrar la transacción
            PointsTransaction transaction = new PointsTransaction(
                clientId,
                TransactionType.SPENT,
                points,
                source,
                description
            );
            pointsTransactionRepository.save(transaction);
            
            return true;
        }
        return false; // Fondos insuficientes
    }

    /**
     * Obtiene el historial de transacciones de un cliente
     */
    public List<PointsTransaction> getTransactionHistory(String clientId) {
        return pointsTransactionRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    /**
     * Obtiene o crea una cuenta de puntos si no existe
     */
    private PointsAccount getOrCreateAccount(String clientId) {
        return pointsAccountRepository.findByClientId(clientId)
            .orElseGet(() -> createPointsAccount(clientId));
    }

    /**
     * Obtiene el balance actual de puntos de un cliente
     */
    public Double getCurrentBalance(String clientId) {
        return getOrCreateAccount(clientId).getCurrentBalance();
    }

    /**
     * Migra puntos existentes desde órdenes cerradas (para migración de datos)
     */
    public void migrateExistingPoints(String clientId, Double totalPoints) {
        PointsAccount account = getOrCreateAccount(clientId);
        if (account.getTotalEarned() == 0.0) { // Solo si no tiene puntos migrados
            account.addPoints(totalPoints);
            pointsAccountRepository.save(account);
            
            // Registrar transacción de migración
            PointsTransaction transaction = new PointsTransaction(
                clientId,
                TransactionType.EARNED,
                totalPoints,
                "MIGRATION",
                "Migración de puntos de órdenes existentes"
            );
            pointsTransactionRepository.save(transaction);
        }
    }

    /**
     * Obtiene la tasa de conversión actual y calcula cuántos puntos se obtienen por un monto
     */
    public Double calculatePointsForAmount(Double amount) {
        double conversionRate = settingsService.getPointsConversionRate();
        return amount / conversionRate;
    }

    /**
     * Obtiene información sobre la configuración actual de puntos
     */
    public Double getCurrentConversionRate() {
        return settingsService.getPointsConversionRate();
    }
}