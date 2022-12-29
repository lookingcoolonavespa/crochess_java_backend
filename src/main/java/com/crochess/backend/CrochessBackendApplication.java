package com.crochess.backend;

import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import com.crochess.backend.models.gameSeek.GameSeek;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class CrochessBackendApplication {

    public static void main(String[] args) throws ClassNotFoundException {
        SpringApplication.run(CrochessBackendApplication.class, args);
    }

    public static SessionFactory sf =
            new Configuration().configure("hibernate.cfg.xml")
                               .addAnnotatedClass(Game.class)
                               .addAnnotatedClass(GameState.class)
                               .addAnnotatedClass(
                                       GameSeek.class)
                               .buildSessionFactory();
}
