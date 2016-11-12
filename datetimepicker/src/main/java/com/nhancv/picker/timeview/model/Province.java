package com.nhancv.picker.timeview.model;


import java.io.Serializable;
import java.util.List;

public class Province implements Serializable {
    public String name;
    public List<City> city;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCity() {
        return city;
    }

    public void setCity(List<City> city) {
        this.city = city;
    }
}
