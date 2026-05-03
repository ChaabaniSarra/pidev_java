package tn.esprit.services;

import tn.esprit.entities.Video;
import tn.esprit.utils.MyDatabase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoService {

    private Connection cnx = MyDatabase.getInstance().getConnection();
    // 📌 1. Sauvegarder fichier dans dossier videos
    public String saveFile(File file) {
        try {
            // 📁 dossier XAMPP
            String folderPath = "C:\\Users\\Mouhamed Tayssir\\Videos";

            File dir = new File(folderPath);
            if (!dir.exists()) {
                dir.mkdirs(); // crée tous les dossiers si nécessaire
            }

            // 📌 nom du fichier
            String fileName = System.currentTimeMillis() + "_" + file.getName();

            // 📌 chemin final
            String fullPath = folderPath + "\\" + fileName;

            File dest = new File(fullPath);

            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 📌 IMPORTANT : on stocke aussi une URL web (optionnel mais recommandé)
            return "videos/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void addVideo(Video v) {
        try {
            String sql = "INSERT INTO video (title, path) VALUES (?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, v.getTitle());
            ps.setString(2, v.getPath());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //the new get video FL
    public List<Video> getAllVideos() {

        List<Video> list = new ArrayList<>();

        String sql = "SELECT * FROM video";
        try{
            //récupère la cnx sans mettre dans les () de try pour éviter qu'elle se ferme
            Connection con = MyDatabase.getInstance().getConnection();
           try( Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)){
               while (rs.next()) {
                   list.add(new Video(
                           rs.getInt("id"),
                           rs.getString("title"),
                           rs.getString("path")));
            }
        }  } catch (Exception e) {
            System.out.println("❌ getVideos: " + e.getMessage());
        }

        return list;
        }

    public List<Video> getLocalVideos(String folderPath) {

        List<Video> list = new ArrayList<>();

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("❌ Folder not found: " + folderPath);
            return list;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.endsWith(".mp4") ||
                        name.endsWith(".m3u8") ||
                        name.endsWith(".webm")
        );

        if (files != null) {
            for (File file : files) {

                list.add(new Video(
                        0,
                        file.getName(),
                        file.getAbsolutePath()
                ));
            }
        }

        return list;
    }

        public void deleteVideo(int id) {

        String sql = "DELETE FROM video WHERE id = ?";

        try (Connection con = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("🗑 Video deleted");

        } catch (Exception e) {
            System.out.println("❌ deleteVideo: " + e.getMessage());
        }
    }
    public boolean existsByPath(String path) {
        try {
            String sql = "SELECT COUNT(*) FROM video WHERE path=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, path);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}