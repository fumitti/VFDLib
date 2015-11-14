package com.fumitti.vfdlib;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fumitti on 2015/05/15.
 */
public class BitmapChar {
    private final char thischar;
    private List<List<Integer>> BitmapData = new ArrayList<>();

    BitmapChar(int x ,int y){
        byte[] euccode = {(byte) (0xA0 + x),(byte) (0xA0 + y)};
        String unicode="";
        try {
            unicode = new String(euccode, "EUC-JP");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        thischar = unicode.charAt(0);
    }

    BitmapChar(byte[] euccode){
        String unicode="";
        try {
            unicode = new String(euccode, "EUC-JP");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        thischar = unicode.charAt(0);
    }

    BitmapChar(char c){
        thischar = c;
    }

    public char getThischar() {
        return thischar;
    }

    public void putBitmap(List<Integer> l){
        BitmapData.add(l);
    }

    public List<List<Integer>> getBitmapData() {
        return BitmapData;
    }

    public void ぷりんと(){
        for (List<Integer> list : getBitmapData()) {
            for (Integer i : list){
                switch (i) {
                    case 0:
                        System.out.print("□□□□");
                        break;
                    case 1:
                        System.out.print("□□□■");
                        break;
                    case 2:
                        System.out.print("□□■□");
                        break;
                    case 3:
                        System.out.print("□□■■");
                        break;
                    case 4:
                        System.out.print("□■□□");
                        break;
                    case 5:
                        System.out.print("□■□■");
                        break;
                    case 6:
                        System.out.print("□■■□");
                        break;
                    case 7:
                        System.out.print("□■■■");
                        break;
                    case 8:
                        System.out.print("■□□□");
                        break;
                    case 9:
                        System.out.print("■□□■");
                        break;
                    case 10://a
                        System.out.print("■□■□");
                        break;
                    case 11://b
                        System.out.print("■□■■");
                        break;
                    case 12://c
                        System.out.print("■■□□");
                        break;
                    case 13://d
                        System.out.print("■■□■");
                        break;
                    case 14://e
                        System.out.print("■■■□");
                        break;
                    case 15://f
                        System.out.print("■■■■");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println("－－－－－－－－－－－－－－－－");
    }
}
