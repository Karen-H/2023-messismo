package com.messismo.bar.Repositories;

import com.messismo.bar.Entities.SettingsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingsHistoryRepository extends JpaRepository<SettingsHistory, Long> {
    List<SettingsHistory> findByKeyOrderByChangedAtDesc(String key);
}