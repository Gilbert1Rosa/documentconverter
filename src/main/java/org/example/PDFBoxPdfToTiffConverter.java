package org.example;

import ch.qos.logback.classic.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PDFBoxPdfToTiffConverter {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PDFBoxPdfToTiffConverter.class);

    private final ProgressCallback processCallback;
    private final ProgressCallback writeCallback;

    public PDFBoxPdfToTiffConverter(ProgressCallback processCallback, ProgressCallback writeCallback) {
        this.processCallback = processCallback;
        this.writeCallback = writeCallback;
    }

    public void convertPdfToMultiPageTiff(List<String> pdfPaths, String outputPath, int dpi) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        for (String pdfPath : pdfPaths) {
            try (PDDocument document = PDDocument.load(new File(pdfPath))) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(
                            pageIndex,
                            dpi,
                            ImageType.RGB
                    );
                    images.add(bufferedImage);
                    processCallback.progress(pageIndex);
                }
            }
        }

        saveAsMultiPageTiff(images, outputPath);
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

        LOGGER.debug("Escribiendo paginas...");
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