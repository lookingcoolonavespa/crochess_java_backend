package com.crochess.backend.main.controllers;

import com.crochess.backend.controllers.GameController;
import com.crochess.backend.daos.GameDao;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.*;

import java.sql.SQLException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class GameControllerTest {

    @Nested
    class post {
        GameController controller = new GameController(new GameDao());

        @Test
        public void persistsGamestateAndGame() throws SQLException {
            Game game = new Game(null, "wfdafa", "bdfdafe", 180, 5, null, null, null, new GameState());

            Game gameResult = controller.insert(game);

            MatcherAssert.assertThat(gameResult, is(game));
        }

        @Test
        public void getsAGame() throws SQLException {
            Game game = controller.get(47);

            MatcherAssert.assertThat(game, not(nullValue()));
        }
    }
}
