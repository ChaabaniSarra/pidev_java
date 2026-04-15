package tn.esprit.services;

import tn.esprit.entities.Stream;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceStream {

    Connection conn = MyDatabase.getInstance().getConnection();

    public List<Stream> getAllStreams() {
        List<Stream> list = new ArrayList<>();

        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM stream");

            while (rs.next()) {
                Stream s = new Stream();
                s.setId(rs.getInt("id"));
                s.setUrl(rs.getString("url"));
                s.setActive(rs.getBoolean("is_active"));
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Stream getActiveStream() {
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM stream WHERE is_active=1 LIMIT 1");

            if (rs.next()) {
                Stream s = new Stream();
                s.setId(rs.getInt("id"));
                s.setUrl(rs.getString("url"));
                s.setActive(true);
                return s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void activate(int id) {
        try {
            conn.createStatement().executeUpdate("UPDATE stream SET is_active=0");

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE stream SET is_active=1 WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}