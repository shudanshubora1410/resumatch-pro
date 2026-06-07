package com.resumatchpro.utility;

import org.springframework.stereotype.Component;

@Component
public class GradeCalculatorUtil {

    public static class GradeResult {
        public String grade;
        public String label;
        public String atsStatus;
        public String atsStatusColor;

        public GradeResult(String grade, String label, String atsStatus, String atsStatusColor) {
            this.grade = grade;
            this.label = label;
            this.atsStatus = atsStatus;
            this.atsStatusColor = atsStatusColor;
        }
    }

    public GradeResult calculateGrade(int score) {
        if (score >= 90) return new GradeResult("A+", "Exceptional Match", "LIKELY TO PASS ATS", "GREEN");
        if (score >= 80) return new GradeResult("A", "Strong Match", "LIKELY TO PASS ATS", "GREEN");
        if (score >= 70) return new GradeResult("B+", "Good Match", "LIKELY TO PASS ATS", "GREEN");
        if (score >= 60) return new GradeResult("B", "Moderate Match", "BORDERLINE – MAY PASS", "ORANGE");
        if (score >= 50) return new GradeResult("C", "Partial Match", "BORDERLINE – MAY PASS", "ORANGE");
        if (score >= 40) return new GradeResult("D", "Weak Match", "LIKELY REJECTED BY ATS", "RED");
        return new GradeResult("F", "Poor Match", "LIKELY REJECTED BY ATS", "RED");
    }
}
