package com.resumatchpro.utility;

import com.resumatchpro.exception.InvalidFileException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FileParserUtil {

    private static final Logger log = LoggerFactory.getLogger(FileParserUtil.class);

    private static final String MIME_PDF = "application/pdf";
    private static final String MIME_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    /**
     * Parse uploaded resume file and extract text.
     * @param file MultipartFile from upload
     * @return ParsedResult containing extracted text, word count, and metadata
     * @throws InvalidFileException for invalid/corrupt files
     */
    public ParsedResult parse(MultipartFile file) throws InvalidFileException {
        validateFile(file);

        String mimeType = file.getContentType();
        String filename = file.getOriginalFilename();
        long startTime = System.currentTimeMillis();

        try {
            String extractedText;
            if (MIME_PDF.equals(mimeType)) {
                extractedText = extractFromPdf(file.getInputStream());
            } else if (MIME_DOCX.equals(mimeType)) {
                extractedText = extractFromDocx(file.getInputStream());
            } else {
                throw new InvalidFileException("Unsupported file type. Only PDF and DOCX are accepted.");
            }

            if (extractedText == null || extractedText.isBlank()) {
                throw new InvalidFileException("Resume appears empty or unreadable. "
                        + "Please ensure it contains text content, not just images.");
            }

            int wordCount = countWords(extractedText);

            if (wordCount < 100) {
                log.warn("Very short resume detected: {} words from {}", wordCount, filename);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Parsed '{}' - {} chars, {} words in {}ms",
                    filename, extractedText.length(), wordCount, elapsed);

            return new ParsedResult(extractedText, wordCount, elapsed,
                    determineFileType(mimeType), filename);

        } catch (InvalidFileException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to parse file '{}': {}", filename, e.getMessage());
            throw new InvalidFileException("Failed to read file. It may be corrupted or password-protected.");
        }
    }

    private void validateFile(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file uploaded.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the 5MB limit.");
        }
        String mimeType = file.getContentType();
        if (!MIME_PDF.equals(mimeType) && !MIME_DOCX.equals(mimeType)) {
            throw new InvalidFileException("Invalid file type: " + mimeType
                    + ". Only PDF and DOCX files are accepted.");
        }
        // Also validate extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (!lower.endsWith(".pdf") && !lower.endsWith(".docx")) {
                throw new InvalidFileException(
                        "Invalid file extension. Only .pdf and .docx files are accepted.");
            }
        }
    }

    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            if (document.isEncrypted()) {
                throw new InvalidFileException(
                        "PDF is encrypted/password-protected. Please upload an unprotected file.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(false);
            stripper.setSuppressDuplicateOverlappingText(true);
            String text = stripper.getText(document);

            // Clean common PDF artifacts
            text = text.replaceAll("\\r\\n", "\n")
                       .replaceAll("\\r", "\n")
                       .replaceAll("(\\n\\s*){4,}", "\n\n\n");

            return text.trim();
        } catch (InvalidPasswordException e) {
            throw new InvalidFileException("PDF is password-protected.");
        }
    }

    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (text != null) {
                text = text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            }
            return text != null ? text.trim() : "";
        }
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private String determineFileType(String mimeType) {
        return MIME_PDF.equals(mimeType) ? "PDF" : "DOCX";
    }

    /**
     * Result wrapper for parsed resume
     */
    public static class ParsedResult {
        private final String extractedText;
        private final int wordCount;
        private final long processingTimeMs;
        private final String fileType;
        private final String originalFilename;

        public ParsedResult(String extractedText, int wordCount,
                            long processingTimeMs, String fileType, String originalFilename) {
            this.extractedText = extractedText;
            this.wordCount = wordCount;
            this.processingTimeMs = processingTimeMs;
            this.fileType = fileType;
            this.originalFilename = originalFilename;
        }

        public String getExtractedText() { return extractedText; }
        public int getWordCount() { return wordCount; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public String getFileType() { return fileType; }
        public String getOriginalFilename() { return originalFilename; }
    }
}