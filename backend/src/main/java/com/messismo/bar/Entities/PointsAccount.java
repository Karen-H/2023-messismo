package com.messismo.bar.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "points_accounts")
public class PointsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    @Column(name = "current_balance", nullable = false)
    private Double currentBalance;

    @Column(name = "total_earned", nullable = false)
    private Double totalEarned;

    @Column(name = "total_spent", nullable = false)
    private Double totalSpent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    public PointsAccount(String clientId) {
        this.clientId = clientId;
        this.currentBalance = 0.0;
        this.totalEarned = 0.0;
        this.totalSpent = 0.0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    public void addPoints(Double points) {
        this.currentBalance += points;
        this.totalEarned += points;
        this.updatedAt = new Date();
    }

    public boolean spendPoints(Double points) {
        if (this.currentBalance >= points) {
            this.currentBalance -= points;
            this.totalSpent += points;
            this.updatedAt = new Date();
            return true;
        }
        return false;
    }
}