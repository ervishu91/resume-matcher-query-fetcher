package org.example.parser;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ResumeParser {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws IOException, TikaException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !(filename.toLowerCase().endsWith(".pdf")
                        || filename.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException(
                    "Only PDF and DOCX files are supported"
            );
        }

        return tika.parseToString(file.getInputStream());
    }
}