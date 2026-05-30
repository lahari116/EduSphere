package com.edusphere.notification.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a styled certificate-of-completion PDF using OpenPDF.
 * The returned byte array is attached to course-completion emails.
 */
@Slf4j
@Component
public class CertificatePdfGenerator {

    private static final Color PURPLE      = new Color(0x4f, 0x46, 0xe5); // indigo-600
    private static final Color DARK_PURPLE = new Color(0x7c, 0x3a, 0xed); // violet-600
    private static final Color GOLD        = new Color(0xf5, 0x9e, 0x0b); // amber-400
    private static final Color DARK        = new Color(0x1e, 0x29, 0x3b); // slate-800
    private static final Color MUTED       = new Color(0x64, 0x74, 0x8b); // slate-500
    private static final Color LIGHT_GRAY  = new Color(0xe2, 0xe8, 0xf0); // slate-200
    private static final Color GREEN       = new Color(0x10, 0xb9, 0x81); // emerald-500
    private static final Color WHITE       = Color.WHITE;

    /**
     * Generate a certificate PDF for the given student and course.
     *
     * @param studentName full name of the student
     * @param courseTitle title of the completed course
     * @return raw PDF bytes
     */
    public byte[] generate(String studentName, String courseTitle) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // A4 landscape for a classic certificate look
            Rectangle pageSize = new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth());
            Document document = new Document(pageSize, 60, 60, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            PdfContentByte canvas = writer.getDirectContent();

            // ── Background fill ──────────────────────────────────────────────
            canvas.setColorFill(WHITE);
            canvas.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            canvas.fill();

            // ── Outer decorative border (double) ─────────────────────────────
            drawBorder(canvas, pageSize, GOLD, 6f, 14f);
            drawBorder(canvas, pageSize, PURPLE, 1.5f, 22f);

            // ── Header banner ────────────────────────────────────────────────
            float bannerH = 80f;
            float bannerY = pageSize.getHeight() - bannerH - 30f;
            canvas.setColorFill(DARK_PURPLE);
            canvas.rectangle(30, bannerY, pageSize.getWidth() - 60, bannerH);
            canvas.fill();

            // ── Fonts ─────────────────────────────────────────────────────────
            BaseFont bfBold  = BaseFont.createFont(BaseFont.HELVETICA_BOLD,   BaseFont.CP1252, false);
            BaseFont bfPlain = BaseFont.createFont(BaseFont.HELVETICA,        BaseFont.CP1252, false);
            BaseFont bfItal  = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, false);

            // ── Header text ───────────────────────────────────────────────────
            float cx = pageSize.getWidth() / 2f;

            // "EduSphere" in banner
            canvas.beginText();
            canvas.setColorFill(WHITE);
            canvas.setFontAndSize(bfBold, 26f);
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "EduSphere Learning Platform",
                    cx, bannerY + 44f, 0);

            canvas.setFontAndSize(bfPlain, 11f);
            canvas.setColorFill(new Color(0xc7, 0xd2, 0xfe)); // indigo-200
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "CERTIFICATE OF COMPLETION",
                    cx, bannerY + 22f, 0);
            canvas.endText();

            // ── Body: "This is to certify that" ──────────────────────────────
            float bodyTop = bannerY - 40f;

            canvas.beginText();
            canvas.setColorFill(MUTED);
            canvas.setFontAndSize(bfPlain, 11f);
            canvas.showTextAligned(Element.ALIGN_CENTER, "This is to certify that", cx, bodyTop, 0);
            canvas.endText();

            // ── Student name ──────────────────────────────────────────────────
            canvas.beginText();
            canvas.setColorFill(DARK);
            canvas.setFontAndSize(bfItal, 34f);
            canvas.showTextAligned(Element.ALIGN_CENTER, studentName, cx, bodyTop - 48f, 0);
            canvas.endText();

            // Underline beneath name
            float nameWidth = bfItal.getWidthPoint(studentName, 34f);
            float lineX = cx - nameWidth / 2f;
            canvas.setColorStroke(GOLD);
            canvas.setLineWidth(1.5f);
            canvas.moveTo(lineX, bodyTop - 54f);
            canvas.lineTo(lineX + nameWidth, bodyTop - 54f);
            canvas.stroke();

            // ── "has successfully completed" ──────────────────────────────────
            canvas.beginText();
            canvas.setColorFill(MUTED);
            canvas.setFontAndSize(bfPlain, 11f);
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "has successfully completed the course", cx, bodyTop - 80f, 0);
            canvas.endText();

            // ── Course title ──────────────────────────────────────────────────
            canvas.beginText();
            canvas.setColorFill(PURPLE);
            canvas.setFontAndSize(bfBold, 20f);
            canvas.showTextAligned(Element.ALIGN_CENTER, courseTitle, cx, bodyTop - 116f, 0);
            canvas.endText();

            // Separator line under course title
            canvas.setColorStroke(LIGHT_GRAY);
            canvas.setLineWidth(1f);
            canvas.moveTo(cx - 200f, bodyTop - 126f);
            canvas.lineTo(cx + 200f, bodyTop - 126f);
            canvas.stroke();

            // ── Issue date ────────────────────────────────────────────────────
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            canvas.beginText();
            canvas.setColorFill(MUTED);
            canvas.setFontAndSize(bfPlain, 10f);
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "Issued on  " + date, cx, bodyTop - 152f, 0);
            canvas.endText();

            // ── Verified badge pill ────────────────────────────────────────────
            float pillW = 180f, pillH = 26f;
            float pillX = cx - pillW / 2f;
            float pillY = bodyTop - 196f;

            canvas.setColorFill(GREEN);
            drawRoundRect(canvas, pillX, pillY, pillW, pillH, 13f);
            canvas.fill();

            canvas.beginText();
            canvas.setColorFill(WHITE);
            canvas.setFontAndSize(bfBold, 10f);
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "✔  Course Completed", cx, pillY + 8f, 0);
            canvas.endText();

            // ── Footer ────────────────────────────────────────────────────────
            canvas.beginText();
            canvas.setColorFill(MUTED);
            canvas.setFontAndSize(bfPlain, 8f);
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    "This certificate was automatically generated by EduSphere Learning Platform.",
                    cx, 40f, 0);
            canvas.endText();

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate certificate PDF for '{}': {}", studentName, e.getMessage(), e);
            throw new RuntimeException("Certificate PDF generation failed", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void drawBorder(PdfContentByte cb, Rectangle page,
                            Color color, float lineWidth, float inset) {
        cb.setColorStroke(color);
        cb.setLineWidth(lineWidth);
        cb.rectangle(inset, inset, page.getWidth() - inset * 2, page.getHeight() - inset * 2);
        cb.stroke();
    }

    /** Approximates a rounded rectangle using Bezier curves. */
    private void drawRoundRect(PdfContentByte cb, float x, float y,
                               float w, float h, float r) {
        float k = 0.5523f * r;
        cb.moveTo(x + r, y);
        cb.lineTo(x + w - r, y);
        cb.curveTo(x + w - r + k, y, x + w, y + k, x + w, y + r);
        cb.lineTo(x + w, y + h - r);
        cb.curveTo(x + w, y + h - r + k, x + w - r + k, y + h, x + w - r, y + h);
        cb.lineTo(x + r, y + h);
        cb.curveTo(x + r - k, y + h, x, y + h - r + k, x, y + h - r);
        cb.lineTo(x, y + r);
        cb.curveTo(x, y + r - k, x + r - k, y, x + r, y);
        cb.closePath();
    }
}
