package org.monarchinitiative.threes.core.pwm;

import java.util.List;

/**
 * This POJO represents a position-weight matrix (PWM). The PWM attributes are:
 * <ul>
 * <li><b>name</b> - name of the PWM</li>
 * <li><b>matrix</b> - internal representation of PWM values used for scoring of nucleotide sequences</li>
 * </ul>
 */
public class PositionWeightMatrix {


    private String name;

    private List<List<Double>> matrix;

    private int exon;

    private int intron;


    public PositionWeightMatrix() {
    }

    public int getExon() {
        return exon;
    }

    public void setExon(int exon) {
        this.exon = exon;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public List<List<Double>> getMatrix() {
        return matrix;
    }


    public void setMatrix(List<List<Double>> matrix) {
        this.matrix = matrix;
    }

    public int getIntron() {
        return intron;
    }

    public void setIntron(int intron) {
        this.intron = intron;
    }
}

