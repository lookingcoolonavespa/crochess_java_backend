package com.crochess.backend.daos;

import com.crochess.backend.CrochessBackendApplication;
import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import com.crochess.backend.models.gameSeek.GameSeek;
import jakarta.persistence.*;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class GameDao {
    private Session ss;
    private Transaction tx;

    public List<Integer> insert(Game game) {
        try {
            ss = CrochessBackendApplication.sf.getCurrentSession();
            tx = ss.beginTransaction();
            System.out.println(game);
            GameState gs =
                    new GameState(game.getId(), System.currentTimeMillis(), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP" +
                            "/RNBQKBNR w KQkq - 0 1", game.getTime() * 1000L, game.getTime() * 1000L, null, null,
                            null, null, game);
            DrawRecord dr = new DrawRecord(game.getId(), false, false, game);
            game.setGameState(gs);
            game.setDrawRecord(dr);
            ss.persist(gs);
            // delete game seeks opened by either player
            String hql = "SELECT gs.id FROM GameSeek gs WHERE gs.seeker = :wId OR gs.seeker = :bId";
            Query query = ss.createQuery(hql);
            query.setParameter("wId", game.getW_id());
            query.setParameter("bId", game.getB_id());
            List<Integer> list = query.getResultList();

            hql = "DELETE FROM GameSeek WHERE seeker = :wId OR seeker = :bId";
            query = ss.createQuery(hql);
            query.setParameter("wId", game.getW_id());
            query.setParameter("bId", game.getB_id());
            query.executeUpdate();
            tx.commit();

            return list;
        } catch (Exception error) {
            System.out.println(error.getLocalizedMessage());
            if (tx != null) tx.rollback();
        } finally {
            if (ss != null) ss.close();
        }

        return null;
    }

    public Game update(int id, Consumer<Game> updater) {
        try {
            ss = CrochessBackendApplication.sf.getCurrentSession();
            Transaction tx = ss.beginTransaction();

            Game game = ss.getReference(Game.class, id);
            updater.accept(game);
            ss.merge(game);

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

    public GameState getState(int id) {
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
