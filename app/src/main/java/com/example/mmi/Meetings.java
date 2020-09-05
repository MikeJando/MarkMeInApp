package com.example.mmi;

public class Meetings
{
        public String classDate;
        public String classID;

        public Meetings(String classDate, String classID) {
            this.classDate = classDate;
            this.classID = classID;
        }

        public String getClassDate() {
            return classDate;
        }
    public String getClassID() {
            return classID;
        }
}

