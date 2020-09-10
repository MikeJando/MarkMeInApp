package com.example.mmi.Teacher;

public class Classes
{
    public String name;
    public String classID;

    public Classes(String name, String classID)
    {

        this.name = name;
        this.classID = classID;
    }

    public String getName() {
        return name;
    }
    public String getClassID() {
        return classID;
    }
}