package tn.esprit.services;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FaceRecognitionService {

    // ─── Face Recognition ───────────────────────
    public static boolean recognizeFace(String enrolledFacePath, Mat currentFace) {
        try {
            if (enrolledFacePath == null || !Files.exists(Paths.get(enrolledFacePath))) return false;

            Mat enrolled = Imgcodecs.imread(enrolledFacePath, 0);
            if (enrolled.empty()) return false;

            // Resize to same size for comparison
            Mat resized = new Mat();
            Imgproc.resize(currentFace, resized, enrolled.size());

            // Compare using normalized cross-correlation
            Mat result = new Mat();
            Imgproc.matchTemplate(resized, enrolled, result, Imgproc.TM_CCOEFF_NORMED);

            double similarity = Core.minMaxLoc(result).maxVal;
            System.out.println("Face similarity: " + similarity);

            // Threshold: 0.5 = 50% match required
            return similarity >= 0.5;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}