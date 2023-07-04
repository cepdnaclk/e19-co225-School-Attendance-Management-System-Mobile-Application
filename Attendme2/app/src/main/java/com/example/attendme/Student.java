package com.example.attendme;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Student {

    private String stdName;
    private boolean assigned;
    private String stdID;

    private Map<String,Boolean> attendance;
    private String studentGrade;
    private String studentSection;
    private int counter;
    private double precentage = 0;
    public Student(){

    }
    public Student(String stdName, boolean assigned, String stdID) {
        this.stdName = stdName;
        this.assigned = assigned;
        this.stdID = stdID;
    }

    public Student(String stdName) {
        this.stdName = stdName;
    }

    public Student(String stdName, String stdID) {
        this.stdName = stdName;
        this.stdID = stdID;
    }

    public Student(String stdID, String grade, String name, String section) {
        this.stdID = stdID;
        this.stdName = name;
        this.studentGrade = grade;
        this.studentSection = section;
        attendance = new HashMap<>();
        counter = 0;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public String getStdName() {
        return stdName;
    }

    public boolean getAssigned(){
        return assigned;
    }

    public String getStdID() {
        return stdID;
    }

    public void setStdID(String stdID) {
        this.stdID = stdID;
    }

    public void addAttendance(String date, boolean value){
        attendance.put(date,value);
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public Map<String, Boolean> getAttendance() {
        return attendance;
    }

    @NonNull
    @Override
    public String toString() {
        return stdName + " is in " + studentSection + " and " + studentGrade + " is " + attendance;
    }

    public void setPrecentage(double precentage) {
        this.precentage = precentage;
    }

    public double getPrecentage() {
        return precentage;
    }

    public void calculateAttendance(){
        int presentCount = 0;
        for (Map.Entry<String, Boolean> entry : attendance.entrySet()) {
            String date = entry.getKey();
            Boolean bool = entry.getValue();
            if(bool){
                presentCount++;
            }
        }

        precentage = presentCount/(counter+0.0);
    }


    public boolean getReport(String presentValue, String gradeValue, String dateValue, String sectionValue) {
        boolean returnVal = false;
        if (presentValue.equals("Present")) {
            if (gradeValue.equals("Any") || gradeValue.equals(studentGrade)) {
                if (check(dateValue, true)) {
                    if (sectionValue.equals(("Any")) || sectionValue.equals(studentSection)) {
                        returnVal = true;
                    }
                }
            }
        } else if (gradeValue.equals("Any") || gradeValue.equals(studentGrade)) {
            if (check(dateValue, false)) {
                if (sectionValue.equals(("Any")) || sectionValue.equals(studentSection)) {
                    returnVal = true;
                }
            }
        }
        return returnVal;
    }

    public String getStudentSection() {
        return studentSection;
    }

    private boolean check(String dateValue, boolean b) {
        boolean checkedVal = false;
        for (Map.Entry<String, Boolean> entry : attendance.entrySet()) {
            String date = entry.getKey();
            Boolean value = entry.getValue();
            if(dateValue.equals(date) && (b == value)){
                checkedVal = true;
                break;
            }
        }
        return checkedVal;
    }
}