package com.example.mmi;

        import java.sql.Connection;
        import java.sql.DriverManager;
        import java.sql.SQLException;

public class DBUtility
{
    public static Connection connect() throws ClassNotFoundException, SQLException {
        Connection con;

        //Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://192.168.1.235:3306/mmi?useSSL=false","msjando","Tuesday8");

        return con;
    }
}