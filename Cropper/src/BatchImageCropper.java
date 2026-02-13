import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BatchImageCropper {

    public static void main(String[] args) {
        // ✅ Input: folder containing 1080p images (relative to the RUN "Working directory")
        // This tries common IntelliJ layouts:
        // 1) <project>/src/data-set
        // 2) <project>/Cropper/src/data-set
        String inputFolder = resolveInputFolder("src/data-set", "Cropper/src/data-set");
        // ✅ Output: folder where cropped images will be saved
        String outputFolder = "output_crops";
        int cropSize = 1024;

        try {
            processFolder(inputFolder, outputFolder, cropSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String resolveInputFolder(String... candidates) {
        Path wd = Paths.get("").toAbsolutePath();
        for (String c : candidates) {
            Path p = wd.resolve(c);
            if (p.toFile().isDirectory()) return p.toString();
        }
        // fallback to first candidate (so the error message shows the intended path)
        return wd.resolve(candidates[0]).toString();
    }

    public static void processFolder(String inputFolder, String outputFolder, int cropSize) throws IOException {
        File inDir = new File(inputFolder);
        System.out.println("📁 Working dir: " + Paths.get("").toAbsolutePath());
        if (!inDir.exists() || !inDir.isDirectory()) {
            System.out.println("❌ Input folder not found: " + inDir.getAbsolutePath());
            System.out.println("   Tip: In IntelliJ Run Configuration, set Working directory to your project folder (or module folder). ");
            return;
        }

        File outDir = new File(outputFolder);
        if (!outDir.exists()) outDir.mkdirs();

        // Loop through all image files in the input folder
        File[] files = inDir.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (files == null || files.length == 0) {
            System.out.println("⚠️ No image files found in " + inputFolder);
            return;
        }

        int totalProcessed = 0;
        for (File file : files) {
            System.out.println("📸 Processing: " + file.getName());
            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                System.out.println("⚠️ Skipping invalid image: " + file.getName());
                continue;
            }

            // Create subfolder for each image’s crops
            File imageOutDir = new File(outDir, file.getName().replaceFirst("\\.[^.]+$", ""));
            imageOutDir.mkdirs();

            cropImage(original, imageOutDir, cropSize );
            totalProcessed++;
        }

        System.out.println("✅ Done! Processed " + totalProcessed + " images.");
    }

    public static void cropImage(BufferedImage original, File outputFolder, int cropSize) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();
        int count = 0;

        for (int y = 0; y + cropSize <= height; y += cropSize) {
            for (int x = 0; x + cropSize <= width; x += cropSize) {
                BufferedImage subImage = original.getSubimage(x, y, cropSize, cropSize);
                File output = new File(outputFolder, "crop_" + count + ".png");
                ImageIO.write(subImage, "png", output);
                count++;
            }
        }

        System.out.println("🧩 Saved " + count + " crops to " + outputFolder.getName());
    }
}