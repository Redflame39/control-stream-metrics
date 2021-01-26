package com.company;

import java.io.IOException;

public class Djilb {
    public static void main(String[] args) throws IOException{
        //Interface.drawTable(Metrix.calculate(Metrix.readCode("Code.txt")));  //    --- постоянная работа с кодом из конкретного файла (удобно при отладке)
        Interface.create();
    }
}
