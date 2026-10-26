package org.example.parser;

import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class ResumeTextNormalizer {

    public String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replace("\u0000", "")
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll(" +", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}