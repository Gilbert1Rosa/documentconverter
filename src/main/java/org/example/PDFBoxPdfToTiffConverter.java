package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PDFBoxPdfToTiffConverter {

    private final ProgressCallback processCallback;
    private final ProgressCallback writeCallback;

    private static final int CHUNK_SIZE = 100;

    private static final ImageType DEFAULT_IMAGE_TYPE = ImageType.RGB;

    public PDFBoxPdfToTiffConverter(ProgressCallback processCallback, ProgressCallback writeCallback) {
        this.processCallback = processCallback;
        this.writeCallback = writeCallback;
    }

    public void convertToPdfMultiPageTiffUsingTemps(String pdfPath, String outputPath, int dpi) throws IOException {
        List<String> tempFiles = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            int pageCount = document.getNumberOfPages();

            if (pageCount < CHUNK_SIZE) {
                return;
            }

            int tempFileIndex = 0;
            for (int page = 0; page < pageCount; page+=CHUNK_SIZE) {
                int end = page + Math.min(CHUNK_SIZE, pageCount - page);
                List<BufferedImage> images = getImagesFromPdfPages(document, page, end, dpi);
                String tempFile = "temp_" + tempFileIndex + ".tiff";
                tempFiles.add(tempFile);
                saveAsMultiPageTiff(images, tempFile);
                tempFileIndex++;
            }
        }

        List<BufferedImage> tempImages = new ArrayList<>();
        for (String tempFile : tempFiles) {
            BufferedImage image = ImageIO.read(new File(tempFile));
            tempImages.add(image);
        }

        saveAsMultiPageTiff(tempImages, outputPath);
        // cleanup temp
    }

    private List<BufferedImage> getImagesFromPdfPages(PDDocument document, int start, int end, int dpi) {
        PDFRenderer renderer = new PDFRenderer(document);
        int pageCount = document.getNumberOfPages();

        if (start > pageCount || end > pageCount) {
            throw new IllegalArgumentException("Start or end out of range");
        }

        List<BufferedImage> images = new ArrayList<>();

        try {
            for (int pageIndex = start; pageIndex < end; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(
                        pageIndex,
                        dpi,
                        DEFAULT_IMAGE_TYPE
                );

                images.add(image);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return images;
    }


    public void convertPdfToMultiPageTiff(String pdfPath, String outputPath, int dpi) throws IOException {

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            System.out.println("Procesando paginas...\n");
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            List<BufferedImage> images = new ArrayList<>();

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(
                        pageIndex,
                        dpi,
                        ImageType.RGB
                );
                images.add(bufferedImage);
                processCallback.progress(pageIndex);
            }

            saveAsMultiPageTiff(images, outputPath);

            System.out.println("Multi-page TIFF created: " + outputPath);

        }
    }

    /**
     * Save multiple BufferedImages as a multi-page TIFF
     */
    private void saveAsMultiPageTiff(List<BufferedImage> images, String outputPath) throws IOException {
        if (images.isEmpty()) {
            throw new IllegalArgumentException("No images to save");
        }

        File outputFile = new File(outputPath);

        ImageIO.write(combineImages(images), "TIFF", outputFile);
    }

    private BufferedImage combineImages(List<BufferedImage> images) {
        int maxWidth = 0;
        int height = 0;

        for (BufferedImage image : images) {
            maxWidth = Math.max(maxWidth, image.getWidth());
            height += image.getHeight();
        }

        BufferedImage combinedImage = new BufferedImage(maxWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, maxWidth, height);

        int currentY = 0;

        int page = 0;
        System.out.println("Escribiendo paginas...\n");
        for (BufferedImage image : images) {
            int x = (maxWidth - image.getWidth()) / 2;
            g2d.drawImage(image, x, currentY, null);
            currentY += image.getHeight();
            writeCallback.progress(page);
            page++;
        }

        g2d.dispose();
        return combinedImage;
    }
}