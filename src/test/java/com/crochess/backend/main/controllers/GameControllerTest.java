//package com.crochess.backend.main.controllers;
//
//import com.crochess.backend.controllers.GameController;
//import com.crochess.backend.daos.GameDao;
//import com.crochess.backend.models.Game;
//import com.crochess.backend.models.GameState;
//import org.hamcrest.MatcherAssert;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import static org.hamcrest.CoreMatchers.*;
//
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {TestConfiguration.class})
//public class GameControllerTest {
//    GameController controller =
//            new GameController(new GameDao(), new SimpMessagingTemplate((message, timeout) -> false));
//
//    @Nested
//    class post {
//        @Test
//        public void persistsGamestateAndGame() {
//            Game game = new Game(8, "osig1kjr", "ru5sajio", 300000, 0);
//            Game gameResult = controller.insert(game);
//
//            MatcherAssert.assertThat(gameResult == null, is(false));
//        }
//
////        @Test
////        public void getsAGame() {
////            Game game = new Game("wfdafa", "bdfdafe", 180, 5);
////
////            controller.insert(game);
////            Game gameResult = controller.get(game.getId());
////
////            MatcherAssert.assertThat(gameResult, not(nullValue()));
////        }
//    }
//
////    @Nested
////    class makeMove {
////        @Test
////        public void makesBasicMove() {
////            Game game = new Game("wfdafa", "bdfdafe", 180, 5);
////
////            Game gameResult = controller.insert(game);
////            MatcherAssert.assertThat(gameResult, is(game));
////
////            controller.makeMove(game.getId(), new GameController.MoveDetails("wfdafa", "e2e4"));
////
////            MatcherAssert.assertThat(controller.get(game.getId())
////                                               .getGameState()
////                                               .getFen(),
////                    is("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0"));
////        }
////    }
//}
