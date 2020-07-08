package com.example.applishopify;

import java.io.Serializable;

public class Address implements Serializable {
    protected String fname;
    protected String lname;
    protected String country;
    protected String city;
    protected String address1;
    protected String address2;

    public Address(String fname,String lname, String country, String city, String address1, String address2){
        this.address1=address1;
        this.address2=address2;
        this.city=city;
        this.country=country;
        this.fname=fname;
        this.lname=lname;
    }

    public String getFname(){
        return fname;
    }

    public String getLname(){
        return lname;
    }

    public String getCountry(){
        return country;
    }

    public String getCity(){
        return city;
    }

    public String getAddress1(){
        return address1;
    }

    public String getAddress2(){
        return address2;
    }

    public boolean isEqual(Address address){
        if (fname.equals(address.getFname())){
            if (lname.equals(address.getLname())){
                if (country.equals(getCountry())){
                    if (city.equals(address.getCity())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString(){
        String text = "address : \n" +
                "first name : " + fname + "\n" +
                "last name : "+ lname + "\n" +
                "city : "+ city + "\n" +
                "country : "+ country + "\n" +
                "first line : "+ address1 + "\n" +
                "second line : "+address2+ "\n";

        return text;
    }
}
