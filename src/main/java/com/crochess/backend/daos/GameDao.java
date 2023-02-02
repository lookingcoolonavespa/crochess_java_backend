package com.crochess.backend.daos;

import com.crochess.backend.CrochessBackendApplication;
import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Consumer;

@Repository
public class GameDao {
    public List<Integer> insert(Game game) {
        Session ss = CrochessBackendApplication.sf.getCurrentSession();
        Transaction tx = null;
        try (ss) {
            tx = ss.beginTransaction();

            game.setTime_stamp_at_turn_start(System.currentTimeMillis());
            game.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            game.setB_time(game.getTime());
            game.setW_time(game.getTime());
            System.out.println(game.getW_time());

            DrawRecord dr = new DrawRecord(game.getId(), false, false, game);
            game.setDrawRecord(dr);
            ss.persist(dr);
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
        }

        return null;
    }

    public Game update(int id, Consumer<Game> updater) {
        Session ss = CrochessBackendApplication.sf.getCurrentSession();
        Transaction tx = null;
        try (ss) {
            tx = ss.beginTransaction();

            Game game = ss.getReference(Game.class, id);
            updater.accept(game);
            ss.merge(game);

            tx.commit();
            return game;
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        }

        return null;
    }

    public void update(Game game) {
        Session ss = CrochessBackendApplication.sf.getCurrentSession();
        Transaction tx = null;
        try (ss) {
            tx = ss.beginTransaction();

            ss.merge(game);

            tx.commit();
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        }
    }

    public Game get(int id) {
        Transaction tx = null;
        try (Session ss = CrochessBackendApplication.sf.getCurrentSession()) {
            tx = ss.beginTransaction();
            Game game = ss.get(Game.class, id);
            System.out.println(game);
            tx.commit();

            return game;
        } catch (Exception error) {
            System.out.println(error);
            if (tx != null) tx.rollback();
        }

        return null;
    }
}
