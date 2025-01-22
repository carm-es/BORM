package com.metaenlace.actualizafirma.propiedades;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Conexion {

    public static Connection obtenerConexion() {

        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource)envCtx.lookup("jdbc/MyDb");

            return ds.getConnection();

        } catch (NamingException | SQLException e) {
            System.err.println("Error estableciendo conexión con base de datos externa." + e.getMessage());
            return null;
        }
    }
    public static void cerrarConexion(Connection con){
        if (con != null) {
            try {
                if (!con.isClosed())
                    con.close();
            } catch (SQLException e) {
                System.err.println("No se ha cerrado la conexión correctamente. Conexion->cerrarConexion(): " + e.getMessage());
            }
        }
    }
}
