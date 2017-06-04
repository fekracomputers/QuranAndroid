package com.fekracomputers.quran.Utilities;

/**
 * Created by nosier on 11/13/2016.
 */

public class Contact {

    //private variables
    int _id;

    String _phone_number;

    // Empty constructor
    public Contact(){

    }
    // constructor
    public Contact(int id, String _phone_number){
        this._id = id;

        this._phone_number = _phone_number;
    }

    // constructor
    public Contact( String _phone_number){

        this._phone_number = _phone_number;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }


    // getting phone number
    public String getPhoneNumber(){
        return this._phone_number;
    }

    // setting phone number
    public void setPhoneNumber(String phone_number){
        this._phone_number = phone_number;
    }
}
