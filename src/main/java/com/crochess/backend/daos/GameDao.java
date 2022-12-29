package com.crochess.backend.daos;

import com.crochess.backend.CrochessBackendApplication;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import com.crochess.backend.models.gameSeek.GameSeek;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ResourceBundle;

@Repository
public class GameDao {
    private final ResourceBundle reader = ResourceBundle.getBundle("env");
    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;
    private Session ss;
    private Transaction tx;

    public Game insert(Game game) {
        try {
            ss = CrochessBackendApplication.sf.getCurrentSession();
            tx = ss.beginTransaction();

            GameState gs =
                    new GameState(game.getId(), System.currentTimeMillis(), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP" +
                            "/RNBQKBNR w KQkq - 0 1", game.getTime(), game.getTime(), null, false, false, game);
            ss.persist(gs);
            // delete game seeks opened by either player
            String hql = "DELETE FROM GameSeek WHERE seeker = :wId OR seeker = :bId";
            Query query = ss.createQuery(hql);
            query.setParameter("wId", game.getW_id());
            query.setParameter("bId", game.getB_id());
            query.executeUpdate();

            tx.commit();
            return game;
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        } finally {
            if (ss != null) ss.close();
        }

        return null;
    }

    public Game get(int id) {
        try {
            ss = CrochessBackendApplication.sf.getCurrentSession();
            Transaction tx = ss.beginTransaction();

            Game game = ss.get(Game.class, id);

            tx.commit();

            return game;
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        } finally {
            if (ss != null) ss.close();
        }

        return null;
    }

    public GameState getState(int id) throws SQLException {
        try {
            ss = CrochessBackendApplication.sf.getCurrentSession();
            Transaction tx = ss.beginTransaction();

            GameState gs = ss.get(GameState.class, id);

            tx.commit();

            return gs;
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        } finally {
            if (ss != null) ss.close();
        }

        return null;
    }
}
