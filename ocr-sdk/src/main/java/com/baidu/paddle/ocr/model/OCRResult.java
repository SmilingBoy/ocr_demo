package com.baidu.paddle.ocr.model;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;

public class OCRResult {
    private List<Point> points;
    private String text;
    private float confidence;
    private int direction;
    private float directionConfidence;

    public OCRResult() {
        this.points = new ArrayList<>();
        this.text = "";
        this.confidence = 0.0f;
        this.direction = 0;
        this.directionConfidence = 0.0f;
    }

    public OCRResult(List<Point> points, String text, float confidence, int direction, float directionConfidence) {
        this.points = points;
        this.text = text;
        this.confidence = confidence;
        this.direction = direction;
        this.directionConfidence = directionConfidence;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void addPoint(int x, int y) {
        this.points.add(new Point(x, y));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getDirectionLabel() {
        return direction == 1 ? "180" : "0";
    }

    public float getDirectionConfidence() {
        return directionConfidence;
    }

    public void setDirectionConfidence(float directionConfidence) {
        this.directionConfidence = directionConfidence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Text: ").append(text).append("\n");
        sb.append("Confidence: ").append(String.format("%.2f", confidence)).append("\n");
        if (points != null && !points.isEmpty()) {
            sb.append("Points: ");
            for (Point p : points) {
                sb.append("(").append(p.x).append(",").append(p.y).append(") ");
            }
            sb.append("\n");
        }
        sb.append("Direction: ").append(getDirectionLabel()).append("\n");
        return sb.toString();
    }
}
