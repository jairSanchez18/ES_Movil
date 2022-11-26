package com.encoding.es_movil.Models;

public class DniModels {
    private String id;
    private String dni;

    public DniModels(String id, String dni) {
        this.id = id;
        this.dni = dni;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
