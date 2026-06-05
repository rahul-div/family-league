package com.familyleague.appconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.familyleague.appconfig.entity.AppConfig;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}
