package org.example;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {

    private static class Params {
        private final String pdfPath;
        private final String outputPath;
        private final int dpi;

        public Params(String pdfPath, String outputPath, int dpi) {
            this.pdfPath = pdfPath;
            this.outputPath = outputPath;
            this.dpi = dpi;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public int getDpi() {
            return dpi;
        }

        public String getPdfPath() {
            return pdfPath;
        }
    }

    public static void main( String[] args ) {
        System.out.println( "Convirtiendo PDF a TIFF (chunks): " );
        Params params = getParams(args);

        try {
            PDFBoxPdfToTiffConverter converter = new PDFBoxPdfToTiffConverter(
                    (int page) -> {
                        System.out.println("Procesando pagina: " + page);
                    },
                    (int page) -> {
                        System.out.println("Escribiendo pagina: " + page);
                    }
            );
            converter.convertPdfToMultiPageTiff(params.getPdfPath(), params.getOutputPath(), params.getDpi());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static Params getParams(String[] args) {
        int dpi;

        try {
            dpi = Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("DPI should be a number!", nfe);
        }

        return new Params(args[0], args[1], dpi);
    }
}
