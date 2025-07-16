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

    public PDFBoxPdfToTiffConverter(ProgressCallback processCallback, ProgressCallback writeCallback) {
        this.processCallback = processCallback;
        this.writeCallback = writeCallback;
    }

    /**
     * Convert PDF to a single multi-page TIFF
     * @param pdfPath Path to the input PDF file
     * @param outputPath Path for the output TIFF file
     * @param dpi Resolution for the output images
     * @throws IOException if file operations fail
     */
    public void convertPdfToMultiPageTiff(String pdfPath, String outputPath, int dpi) throws IOException {
        PDDocument document = PDDocument.load(new File(pdfPath));

        System.out.println("Procesando paginas...\n");
        try {
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

        } finally {
            document.close();
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

        ImageIO.write(combinedImage, "TIFF", outputFile);
    }
}