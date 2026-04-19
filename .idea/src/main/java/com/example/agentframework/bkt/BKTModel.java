package com.example.agentframework.bkt;

import lombok.Data;

@Data
public class BKTModel {
    private double pL0;
    private double pT;
    private double pG;
    private double pS;
    private double pL;

    public BKTModel() {
        this.pL0 = 0.3;
        this.pT = 0.1;
        this.pG = 0.2;
        this.pS = 0.1;
        this.pL = pL0;
    }

    public BKTModel(double pL0, double pT, double pG, double pS) {
        this.pL0 = pL0;
        this.pT = pT;
        this.pG = pG;
        this.pS = pS;
        this.pL = pL0;
    }

    public double update(boolean correct) {
        double pLGivenObservation;

        if (correct) {
            double pCorrectAndLearned = pL * (1 - pS);
            double pCorrectAndNotLearned = (1 - pL) * pG;
            double pCorrect = pCorrectAndLearned + pCorrectAndNotLearned;

            if (pCorrect == 0) pCorrect = 1e-10;
            pLGivenObservation = pCorrectAndLearned / pCorrect;
        } else {
            double pIncorrectAndLearned = pL * pS;
            double pIncorrectAndNotLearned = (1 - pL) * (1 - pG);
            double pIncorrect = pIncorrectAndLearned + pIncorrectAndNotLearned;

            if (pIncorrect == 0) pIncorrect = 1e-10;
            pLGivenObservation = pIncorrectAndLearned / pIncorrect;
        }

        pL = pLGivenObservation + (1 - pLGivenObservation) * pT;
        pL = Math.min(1.0, Math.max(0.0, pL));

        return pL;
    }

    public double predict(boolean correct) {
        if (correct) {
            return pL * (1 - pS) + (1 - pL) * pG;
        } else {
            return pL * pS + (1 - pL) * (1 - pG);
        }
    }

    public double getMasteryLevel() {
        return pL;
    }

    public boolean isMastered(double threshold) {
        return pL >= threshold;
    }

    public String getMasteryLabel() {
        if (pL >= 0.95) return "精通";
        if (pL >= 0.85) return "熟练";
        if (pL >= 0.7) return "掌握";
        if (pL >= 0.5) return "学习中";
        if (pL >= 0.3) return "初步了解";
        return "未掌握";
    }

    public int estimateProblemsToMastery(double threshold) {
        if (pL >= threshold) return 0;

        BKTModel simModel = new BKTModel(pL0, pT, pG, pS);
        simModel.setPL(pL);

        int count = 0;
        int maxIterations = 50;

        while (simModel.getMasteryLevel() < threshold && count < maxIterations) {
            simModel.update(true);
            count++;
        }

        return count;
    }

    public BKTModel copy() {
        BKTModel copy = new BKTModel(pL0, pT, pG, pS);
        copy.setPL(pL);
        return copy;
    }
}
