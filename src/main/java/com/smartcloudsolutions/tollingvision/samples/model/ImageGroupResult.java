package com.smartcloudsolutions.tollingvision.samples.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.smartcloudsolutions.tollingvision.EventResult;
import com.smartcloudsolutions.tollingvision.SearchResponse;

/**
 * Represents the analysis results for a group of images.
 * Contains both the aggregated EventResult and individual SearchResponse data
 * per image.
 */
public class ImageGroupResult {
    private final String bucket;
    private final String frontNames;
    private final String rearNames;
    private final String overNames;
    private final String frontPlate;
    private final String frontAlt;
    private final String rearPlate;
    private final String rearAlt;
    private final String mmr;
    private final String mmrAlt;
    private final List<Path> allImagePaths;
    private final EventResult eventResult;
    private final Map<Path, SearchResponse> imageAnalysisData;

    /**
     * Constructor for real analysis result.
     * 
     * @param bucket          bucket identifier
     * @param eventResult     the analysis result
     * @param imagePaths      list of image paths
     * @param frontPattern    pattern for front images
     * @param rearPattern     pattern for rear images
     * @param overviewPattern pattern for overview images
     */
    public ImageGroupResult(String bucket, EventResult eventResult, List<Path> imagePaths,
            String frontPattern, String rearPattern, String overviewPattern) {
        this.bucket = bucket;
        this.eventResult = eventResult;
        this.allImagePaths = imagePaths;

        Pattern fp = Pattern.compile(frontPattern);
        Pattern rp = Pattern.compile(rearPattern);
        Pattern op = Pattern.compile(overviewPattern);

        this.frontNames = filterNames(imagePaths, fp);
        this.rearNames = filterNames(imagePaths, rp);
        this.overNames = filterNames(imagePaths, op);

        // Format plate and MMR data
        this.frontPlate = eventResult.hasFrontPlate() ? formatPlate(eventResult.getFrontPlate()) : "";
        this.frontAlt = eventResult.getFrontPlateAlternativeList().stream()
                .map(ImageGroupResult::formatPlate)
                .collect(Collectors.joining("|"));
        this.rearPlate = eventResult.hasRearPlate() ? formatPlate(eventResult.getRearPlate()) : "";
        this.rearAlt = eventResult.getRearPlateAlternativeList().stream()
                .map(ImageGroupResult::formatPlate)
                .collect(Collectors.joining("|"));
        this.mmr = eventResult.hasMmr() ? formatMmr(eventResult.getMmr()) : "";
        this.mmrAlt = eventResult.getMmrAlternativeList().stream()
                .map(ImageGroupResult::formatMmr)
                .collect(Collectors.joining("|"));

        // Initialize map for individual image analysis data
        this.imageAnalysisData = new HashMap<>();
    }

    /**
     * Constructor for log entries.
     * 
     * @param bucket  bucket identifier
     * @param message log message
     */
    public ImageGroupResult(String bucket, String message) {
        this.bucket = bucket + " - " + message;
        this.frontNames = "";
        this.rearNames = "";
        this.overNames = "";
        this.frontPlate = "";
        this.frontAlt = "";
        this.rearPlate = "";
        this.rearAlt = "";
        this.mmr = "";
        this.mmrAlt = "";
        this.allImagePaths = Collections.emptyList();
        this.eventResult = null;
        this.imageAnalysisData = new HashMap<>();
    }

    // Getters
    /**
     * Gets the bucket identifier.
     * 
     * @return the bucket identifier
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Gets the front image names.
     * 
     * @return the front image names
     */
    public String getFrontNames() {
        return frontNames;
    }

    /**
     * Gets the rear image names.
     * 
     * @return the rear image names
     */
    public String getRearNames() {
        return rearNames;
    }

    /**
     * Gets the overview image names.
     * 
     * @return the overview image names
     */
    public String getOverNames() {
        return overNames;
    }

    /**
     * Gets the front plate information.
     * 
     * @return the front plate information
     */
    public String getFrontPlate() {
        return frontPlate;
    }

    /**
     * Gets the front plate alternatives.
     * 
     * @return the front plate alternatives
     */
    public String getFrontAlt() {
        return frontAlt;
    }

    /**
     * Gets the rear plate information.
     * 
     * @return the rear plate information
     */
    public String getRearPlate() {
        return rearPlate;
    }

    /**
     * Gets the rear plate alternatives.
     * 
     * @return the rear plate alternatives
     */
    public String getRearAlt() {
        return rearAlt;
    }

    /**
     * Gets the MMR (Make, Model, Recognition) information.
     * 
     * @return the MMR information
     */
    public String getMmr() {
        return mmr;
    }

    /**
     * Gets the MMR alternatives.
     * 
     * @return the MMR alternatives
     */
    public String getMmrAlt() {
        return mmrAlt;
    }

    /**
     * Gets all image paths.
     * 
     * @return the list of all image paths
     */
    public List<Path> getAllImagePaths() {
        return allImagePaths;
    }

    /**
     * Gets the event result.
     * 
     * @return the event result
     */
    public EventResult getEventResult() {
        return eventResult;
    }

    /**
     * Gets the image analysis data map.
     * 
     * @return the map of image paths to SearchResponse data
     */
    public Map<Path, SearchResponse> getImageAnalysisData() {
        return imageAnalysisData;
    }

    /**
     * Adds individual image analysis data.
     * 
     * @param imagePath      the image path
     * @param searchResponse the SearchResponse data for this image
     */
    public void addImageAnalysis(Path imagePath, SearchResponse searchResponse) {
        imageAnalysisData.put(imagePath, searchResponse);
    }

    // Helper methods
    private static String filterNames(List<Path> list, Pattern pattern) {
        return list.stream()
                .filter(f -> pattern.matcher(f.getFileName().toString()).find())
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.joining("|"));
    }

    private static String formatPlate(com.smartcloudsolutions.tollingvision.Plate plate) {
        return String.format("%s %s-%s %s %d%% (text: %d%%|state: %d%%)",
                plate.getText(), plate.getCountry(), plate.getState(), plate.getCategory(),
                plate.getConfidence(), plate.getTextConfidence(), plate.getPlateTypeConfidence());
    }

    private static String formatMmr(com.smartcloudsolutions.tollingvision.Mmr mmr) {
        return String.format(
                "%s %s (generation: %s|category: %s|body type: %s|view point: %s|color: %s|standard color: %s|dimensions: %s)",
                mmr.getMake(), mmr.getModel(), mmr.getGeneration(), mmr.getCategory(),
                mmr.getBodyType(), mmr.getViewPoint(), mmr.getColorName(), mmr.getStandardColorName(),
                mmr.getDimensions() != null ? mmr.getDimensions().getWidth() + "x" +
                        mmr.getDimensions().getHeight() + "x" + mmr.getDimensions().getLength() : "N/A");
    }
}