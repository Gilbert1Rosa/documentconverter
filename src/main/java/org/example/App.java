package org.example;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(App.class);

    private static class Params {
        private final List<String> pdfPaths;
        private final String outputPath;
        private final int dpi;

        public Params(List<String> pdfPaths, String outputPath, int dpi) {
            this.pdfPaths = pdfPaths;
            this.outputPath = outputPath;
            this.dpi = dpi;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public int getDpi() {
            return dpi;
        }

        public List<String> getPdfPaths() {
            return pdfPaths;
        }
    }

    public static void main( String[] args ) {
        LOGGER.debug( "Convirtiendo PDF a TIFF (chunks): " );
        Params params = getParams(args);

        try {
            PDFBoxPdfToTiffConverter converter = new PDFBoxPdfToTiffConverter(
                    (int page) -> {
                        LOGGER.debug("Procesando pagina: " + page);
                    },
                    (int page) -> {
                        LOGGER.debug("Escribiendo pagina: " + page);
                    }
            );
            converter.convertPdfToMultiPageTiff(params.getPdfPaths(), params.getOutputPath(), params.getDpi());
        } catch (IOException ioe) {
            LOGGER.error("IO Error", ioe);
        }
    }

    private static Params getParams(String[] args) {
        List<String> paths = new ArrayList<>();
        String output;
        int dpi;

        try {
            if (args.length == 3) {
                paths.add(args[0]);
                output = args[1];
                dpi = Integer.parseInt(args[2]);
            } else if (args.length > 3) {
                Collections.addAll(paths, Arrays.copyOfRange(args, 0, args.length - 2));
                output = args[args.length - 2];
                dpi = Integer.parseInt(args[args.length - 1]);
            } else {
                throw new IllegalArgumentException("Error with parameters!");
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("DPI should be a number!", nfe);
        }

        return new Params(paths, output, dpi);
    }
}
