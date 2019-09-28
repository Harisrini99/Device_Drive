package com.example.wanted.wanted;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Wanted on 16-01-2018.
 */

public class sendV
{
    private static String value;
    private static String em;
    private  static  String nm;
    private static boolean use;
    private static boolean check;
    private ArrayList<String> arrayList;

    public void getU (boolean use)
    {
        this.use = use;
    }

    public boolean setU()
    {
        return use;
    }




    public void getC(boolean value)
    {
        this.check = value;
    }

    public boolean setC()
    {
        return check;
    }


    public void getV(String a)
    {
        value = a;

    }

    public String setV()
    {
        return value;
    }

    public void getE(String a)
    {
        em = a;

    }

    public String setE()
    {
        return em;
    }


    public void getN(String a)
    {
        nm = a;

    }

    public String setN()
    {
        return nm;
    }


    public void getS(ArrayList<String> a)
    {

        arrayList = a;
    }

    public ArrayList<String> setS()
    {
        return arrayList;
    }

}
