package org.example;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        System.out.println( "Convirtiendo PDF a TIFF (chunks): " );

        try {
            PDFBoxPdfToTiffConverter converter = new PDFBoxPdfToTiffConverter(
                    (int page) -> {
                        System.out.println("Procesando pagina: " + page);
                    },
                    (int page) -> {
                        System.out.println("Escribiendo pagina: " + page);
                    }
            );
            converter.convertPdfToMultiPageTiff("input3.pdf", "output3.tiff", 300);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
