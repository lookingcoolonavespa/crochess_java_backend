package com.crochess.backend.daos_and_repos;

import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ResourceBundle;

@Repository
public class GameDao {
    private final ResourceBundle reader = ResourceBundle.getBundle("env");
    Connection conn;
    PreparedStatement pst;

    public Game insert(Game game) throws SQLException {
        try {
            conn = DriverManager.getConnection(reader.getString("DB_URL"), "DB_USER", "DB_PASSWORD");

            String query =
                    "INSERT INTO crochess.game (id, w_id, b_id, time, increment) VALUES (DEFAULT, ? , ? , ? ,?)";
            pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, game.getW_id());
            pst.setString(2, game.getB_id());
            pst.setInt(3, game.getTime());
            pst.setInt(4, game.getIncrement());

            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next()) {
                int gameId = rs.getInt(1);
                // creating game state
                query =
                        "INSERT INTO crochess.gamestate (id, time_stamp_at_turn_start, fen, w_time, b_time, " +
                                "w_draw, b_draw, game_id) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(query);
                pst.setLong(1, System.currentTimeMillis());
                pst.setString(2, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                pst.setInt(3, game.getTime());
                pst.setInt(4, game.getTime());
                pst.setBoolean(5, false);
                pst.setBoolean(6, false);
                pst.setInt(7, gameId);
                pst.executeUpdate();

                // delete game seeks opened by either player
                query = "DELETE FROM crochess.gameseeks WHERE seeker = ?";
                pst = conn.prepareStatement(query);
                pst.setString(1, game.getW_id());
                pst.executeUpdate();

                query = "DELETE FROM crochess.gameseeks WHERE seeker = ?";
                pst = conn.prepareStatement(query);
                pst.setString(1, game.getB_id());
                pst.executeUpdate();

                rs.close();
                pst.close();
                conn.close();

                game.setId(gameId);

                return game;
            }
        } catch (Exception error) {
            System.out.println(error);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (pst != null) {
                pst.close();
            }
        }

        return null;
    }

    public Game get(int id) throws SQLException {
        try {
            conn = DriverManager.getConnection(reader.getString("DB_URL"), "DB_USER", "DB_PASSWORD");

            String query =
                    "SELECT * FROM crochess.game WHERE id = ?";
            pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            rs.next();

            Game game = new Game();

            GameState gs = new GameState();
            gs.setId(rs.getInt("id"));
            gs.setTime_stamp_at_turn_start(rs.getLong("time_stamp_at_turn_start"));
            gs.setFen(rs.getString("fen"));
            gs.setHistory(rs.getString("history"));
            gs.setB_draw(rs.getBoolean("b_draw"));
            gs.setW_draw(rs.getBoolean("w_draw"));
            gs.setB_time(rs.getLong("b_time"));
            gs.setW_time(rs.getLong("w_time"));

            rs.close();
            pst.close();
            conn.close();

            return gs;
        } catch (Exception error) {
            System.out.println(error);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (pst != null) {
                pst.close();
            }
        }

        return null;
    }

    public GameState getState(int id) throws SQLException {
        try {
            conn = DriverManager.getConnection(reader.getString("DB_URL"), "DB_USER", "DB_PASSWORD");

            String query =
                    "SELECT * FROM crochess.gamestate WHERE game_id = ?";
            pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            rs.next();

            GameState gs = new GameState();
            gs.setId(rs.getInt("id"));
            gs.setTime_stamp_at_turn_start(rs.getLong("time_stamp_at_turn_start"));
            gs.setFen(rs.getString("fen"));
            gs.setHistory(rs.getString("history"));
            gs.setB_draw(rs.getBoolean("b_draw"));
            gs.setW_draw(rs.getBoolean("w_draw"));
            gs.setB_time(rs.getLong("b_time"));
            gs.setW_time(rs.getLong("w_time"));

            rs.close();
            pst.close();
            conn.close();

            return gs;
        } catch (Exception error) {
            System.out.println(error);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (pst != null) {
                pst.close();
            }
        }

        return null;
    }
}
