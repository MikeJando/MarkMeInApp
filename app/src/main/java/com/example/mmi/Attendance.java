package com.example.mmi;

public class Attendance
{
    public String id;
    public String late;
    public String note;
    public String present;

    public Attendance(String id, String late, String note,String present)
    {
        this.id = id;
        this.late = late;
        this.note = note;
        this.present = present;
    }

    public String getId() { return id; }

    public String getLate() { return late; }

    public String getNote() { return note; }

    public String getPresent() { return present; }

    public void setLate(String late) { this.late = late; }

    public void setNote(String note) { this.note = note; }

    public void setPresent(String present) { this.present = present; }
}
