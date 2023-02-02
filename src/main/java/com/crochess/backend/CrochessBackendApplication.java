package com.crochess.backend;

import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.gameSeek.GameSeek;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrochessBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrochessBackendApplication.class, args);
    }

    public static SessionFactory sf =
            new Configuration().configure("hibernate.cfg.xml")
                               .setProperty("hibernate.connection.url", System.getenv("DB_URL"))
                               .setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"))
                               .setProperty(
                                       "hibernate.connection.password", System.getenv("DB_PASSWORD"))
                               .addAnnotatedClass(Game.class)
                               .addAnnotatedClass(
                                       GameSeek.class)
                               .addAnnotatedClass(DrawRecord.class)
                               .buildSessionFactory();
}
