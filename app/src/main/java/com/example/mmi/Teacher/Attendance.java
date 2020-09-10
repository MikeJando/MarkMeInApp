package com.example.mmi.Teacher;

public class Attendance
{
    public String id;
    public String name;
    public String late;
    public String note;
    public String present;

    public Attendance(String id, String name, String late, String note,String present)
    {
        this.id = id;
        this.name = name;
        this.late = late;
        this.note = note;
        this.present = present;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getLate() { return late; }

    public String getNote() { return note; }

    public String getPresent() { return present; }

    public void setLate(String late) { this.late = late; }

    public void setNote(String note) { this.note = note; }

    public void setPresent(String present) { this.present = present; }
}
