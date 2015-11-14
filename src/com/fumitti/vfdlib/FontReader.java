package com.fumitti.vfdlib;

import javax.print.attribute.standard.MediaSize;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fumitti on 2015/05/15.
 */
public class FontReader {

    Map<Character,BitmapChar> FontData = new HashMap<>();

    public void readHEX(String uri){
        System.out.println("HEX BitmapFont Read...");
        File file = new File(uri);
        boolean skip = true;
        boolean bitmap = false;
        BitmapChar workChar=null;
        try {

            for (String s : Files.readAllLines(file.toPath())) {
                String[] split = s.split(":");
                workChar = new BitmapChar((char)Integer.parseInt(split[0],16));
                int cut = 2;
                if (split[1].length() == 64){
                    cut = 4;
                }
                for (int i = 1; i < 17; i++) {
                    List<Integer> l = new ArrayList<>();
                    String substring = split[1].substring((i - 1) * cut, (i * cut));
                    for (char c : substring.toUpperCase().toCharArray()) {
                        l.add(Integer.parseInt(String.valueOf(c),16));
                    }
                    workChar.putBitmap(l);
                }
                FontData.put(workChar.getThischar(), workChar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(FontData.size()+"Characters Loaded.");
    }

    public void readBDF(String uri){
        System.out.println("BDF BitmapFont Read...");
        File file = new File(uri);
        boolean skip = true;
        boolean bitmap = false;
        BitmapChar workChar=null;
        try {
            for (String s : Files.readAllLines(file.toPath())) {
                if(s.startsWith("STARTCHAR")){
                    skip = false;
                    if(s.contains("-")) {
                        String[] JISCodes = s.split(" ")[1].split("-");
                        byte[] euccode = {(byte) (0xA0 + Integer.parseInt(JISCodes[0])),(byte) (0xA0 + Integer.parseInt(JISCodes[1]))};
                        workChar = new BitmapChar(euccode);
                    }else if(s.contains("U+")){
                        char Unicode = (char)Integer.parseInt(s.split(" ")[1].split("U+")[1],16);
                        workChar = new BitmapChar(Unicode);
                    }else{
                        String JISCode = s.split(" ")[1];
                        byte[] euccode = {(byte) (Integer.parseInt(JISCode.substring(0, 2), 16)), (byte) (Integer.parseInt(JISCode.substring(2, 4), 16))};
                        workChar = new BitmapChar(new String(euccode, "EUC-JP").charAt(0));
                    }
                }
                if(s.startsWith("ENDCHAR")){
                    skip = true;
                    bitmap = false;
                    if (workChar != null) {
                        FontData.put(workChar.getThischar(), workChar);
                        //System.out.println("Character \""+workChar.getThischar()+"\" Registed.");
                        workChar=null;
                    }
                }
                if(skip){
                    continue;
                }
                if(s.startsWith("BITMAP")){
                    bitmap = true;
                    continue;
                }
                if(bitmap){
                    List<Integer> l = new ArrayList<>();
                    for (char c : s.toUpperCase().toCharArray()) {
                        l.add(Integer.parseInt(String.valueOf(c),16));
                    }
                    workChar.putBitmap(l);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(FontData.size()+"Characters Loaded.");
    }
}
