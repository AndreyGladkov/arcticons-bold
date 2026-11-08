package com.donnnno.arcticons.helper;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BoldIconRenderer {

    private static final float ICON_SIZE_PX = 256f;
    // Icons without an explicit stroke-width rely on the SVG default of 1;
    // explicit 0 marks filled shapes and must stay unstroked.
    private static final Pattern UNIT_STROKE_WIDTH = Pattern.compile("stroke-width(:\\s*|=\")1(?![0-9.])");
    private static final Pattern SVG_OPENING_TAG = Pattern.compile("<svg\\b");

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: BoldIconRenderer <svg dir> <webp dir> <stroke width>");
            System.exit(1);
        }
        Path svgDir = Path.of(args[0]);
        Path webpDir = Files.createDirectories(Path.of(args[1]));
        String strokeWidth = args[2];

        int failures = renderAll(listSvgs(svgDir), webpDir, strokeWidth);
        if (failures > 0) {
            System.exit(1);
        }
    }

    static String withStrokeWidth(String svg, String strokeWidth) {
        String scaled = UNIT_STROKE_WIDTH.matcher(svg).replaceAll(match -> "stroke-width" + match.group(1) + strokeWidth);
        return SVG_OPENING_TAG.matcher(scaled).replaceFirst("<svg stroke-width=\"" + strokeWidth + "\"");
    }

    private static List<Path> listSvgs(Path svgDir) throws IOException {
        try (Stream<Path> files = Files.list(svgDir)) {
            return files.filter(path -> path.toString().endsWith(".svg")).toList();
        }
    }

    private static int renderAll(List<Path> svgs, Path webpDir, String strokeWidth) {
        AtomicInteger failures = new AtomicInteger();
        svgs.parallelStream().forEach(svg -> {
            Path webp = webpDir.resolve(svg.getFileName().toString().replace(".svg", ".webp"));
            try {
                renderWebp(withStrokeWidth(Files.readString(svg), strokeWidth), webp);
            } catch (Exception e) {
                failures.incrementAndGet();
                System.err.println("Failed to render " + svg.getFileName() + ": " + e.getMessage());
            }
        });
        System.out.printf("Rendered %d of %d icons with stroke width %s%n", svgs.size() - failures.get(), svgs.size(), strokeWidth);
        return failures.get();
    }

    private static void renderWebp(String svg, Path webp) throws Exception {
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, ICON_SIZE_PX);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, ICON_SIZE_PX);
        try (ByteArrayOutputStream png = new ByteArrayOutputStream()) {
            transcoder.transcode(new TranscoderInput(new StringReader(svg)), new TranscoderOutput(png));
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
            if (image == null || !ImageIO.write(image, "webp", webp.toFile())) {
                throw new IOException("no WebP writer available or empty image");
            }
        }
    }
}
