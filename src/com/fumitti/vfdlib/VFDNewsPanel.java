package com.fumitti.vfdlib;

import com.fumitti.vfdlib.Reader.*;
import com.fumitti.vfdlib.Writer.OneWindowWriter;
import com.fumitti.vfdlib.Writer.scrollWriter;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fumitti on 2015/05/14.
 */
public class VFDNewsPanel {
    public static VFDLib vfd;
    private static FontReader jiskan;
    private static FontReader uniFont;
    public static Thread writing = null;
    private static ArrayList<ReaderInterface> readerlist = new ArrayList<>();
    private static FontReader b16;

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                HashSet<CommPortIdentifier> availableSerialPorts = getAvailableSerialPorts();
                for (CommPortIdentifier cpi : availableSerialPorts) {
                    System.out.println(cpi.getName() + ":" + cpi.getPortType());
                }
                //System.exit(0);
                vfd = new VFDLib("COM7", 115200);
            } else {
                System.out.println("Using Port:" + args[0]);
                vfd = new VFDLib(args[0], 115200);
            }
            setupAnime();
            vfd.init();
            vfd.setModeHorScroll();
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("VFDNewsPanelSystem booting...");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("JapaneseFont loading...");
            jiskan = new FontReader();
            jiskan.readBDF("jiskan16-1990.bdf");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->JISKAN Font loaded.");
            b16 = new FontReader();
            b16.readBDF("b16.bdf");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->B16 Font loaded.");
            uniFont = new FontReader();
            uniFont.readHEX("unifont.hex");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->HEXType UNICODE Font loaded.");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("NewsSource Setup...");
            System.out.println("init Reader...");
            readerlist.add(new gooTrainInfo());
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->Train Traffic Information loaded.");
            readerlist.add(new twitterUN_NERV());
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->@UN_Nerv TwitterCollector loaded.");
            readerlist.add(new timeSignal());
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->Time Signal System loaded.");
            NowPlaying nowPlaying = new NowPlaying();
            readerlist.add(nowPlaying);
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->NowPlayingInfo System loaded.");
            DBReader dbReader = new DBReader();
            readerlist.add(dbReader);
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("->DBReader System loaded.");
            vfd.cursor_return();
            vfd.cursor_carriagereturn();
            vfd.print("All module loaded. System Startup...");
            Thread.sleep(2500);
            vfd.init();
            vfd.clear();
            vfd.setWriteWindowMode(true);
            vfd.cursor_set(280, 0);
            while (true) {
                try {
                    queput();
                } catch (IllegalStateException e) {
                    continue;
                }
                if (false) {
                    break;
                }
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static LinkedHashMap<ReaderInterface, ArrayList<String>> Que = new LinkedHashMap<>();
    public static ArrayList<String> newsQue = new ArrayList<>();
    public static ArrayList<String> prioQue = new ArrayList<>();
    private static boolean busy = true;

    private static void queput() {
        if (writing == null) {
        } else {
            try {
                writing.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String text;
        boolean prio = false;
        try {
            if (!prioQue.isEmpty()) {
                text = prioQue.get(0);
                prioQue.remove(0);
                prio = true;
                busy = true;
            } else if (!newsQue.isEmpty()) {
                text = newsQue.get(0);
                newsQue.remove(0);
                busy = true;
            } else if (!Que.isEmpty()) {
                for (ReaderInterface readerInterface : Que.keySet()) {
                    newsQue.addAll(Que.remove(readerInterface).stream().collect(Collectors.toList()));
                    break;
                }
                text = newsQue.get(0);
                newsQue.remove(0);
                busy = true;
            } else {
                try {
                    if (busy) {
                        try {
                            vfd.clear();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    showCalender();
                    busy = false;
                } catch (InterruptedException e) {
                }
                return;
                //TODO LowPrioなNewsを表示させたい
            }
        } catch (IndexOutOfBoundsException ex) {
            newsQue.add(" ");
            text = newsQue.get(0);
            newsQue.remove(0);
        }
        List<String> intl1 = makeybuffer(text);
        scrollWriter writer = new scrollWriter(intl1, prio);
        Thread runner = new Thread(writer);
        if (writing == null) {
            writing = runner;
            writing.start();
        } else {
            try {
                writing.join();
                writing = runner;
                writing.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> makeybuffer(String text) {
        ArrayList<String> ybuf = new ArrayList<>();
        List<String> intl = new ArrayList<>();
        for (char c2 : text.toCharArray()) {
            try {
                String last = "";
                BitmapChar bitmapChar = jiskan.FontData.get(c2);
                if (bitmapChar == null) {
                    bitmapChar = b16.FontData.get(c2);
                }
                if (bitmapChar == null) {
                    bitmapChar = uniFont.FontData.get(c2);
                }
                int xsize = bitmapChar.getBitmapData().get(0).size() * 4;
                int ysize = bitmapChar.getBitmapData().size();
                for (List<Integer> list : bitmapChar.getBitmapData()) {
                    String line = "";
                    for (Integer data : list) {
                        line += String.format("%04d", Integer.parseInt(Integer.toBinaryString(data)));
                    }
                    intl.add(line);
                }

                for (int i = 0; i < xsize; i++) {
                    String yline = "";
                    for (String line : intl) {
                        char c = line.charAt(i);
                        if (c == '0') {
                            yline += "0";
                        } else {
                            yline += "1";
                        }
                    }

                    int v = ysize / 8;
                    int n = ysize % 8;
                    if (n != 0) {
                        v++;
                    }
                    String s = "";
                    for (int j = 0; j < v; j++) {
                        int l = (j + 1) * 8;
                        if (j == v) {
                            if (n == 0)
                                continue;
                            l = j * 8 + n;
                        }
                        int st = j * 8;
                        if (st != 0) {
                            st--;
                        }
                        s += yline.substring(st, l);
                    }
                    ybuf.add(s);
                    last = s;
                }
                intl.clear();
                if (last != "00000000000000000")
                    ybuf.add("00000000000000000");
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return ybuf;
    }

    private static boolean colon = true;
    private static int anime = 0;
    private static ArrayList<ArrayList<String>> animel = new ArrayList<>();

    private static void setupAnime() {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add("(´ー`)y-~~");
        tmp.add("(´ー`)y-~ ");
        animel.add((ArrayList<String>) tmp.clone());
        tmp = new ArrayList<>();
        tmp.add("ヽ( `Д´)ノ");
        tmp.add(". ヽ`Д´)  ");
        tmp.add("　( ヽ`Д)  ");
        tmp.add("　(　 ヽ`)  ");
        tmp.add("　(　　 ヽ  ");
        tmp.add("ヽ( 　　 )ノ");
        tmp.add(".ヽ 　　 )  ");
        tmp.add("　(ヽ　  )  ");
        tmp.add("  (Д´ヽ)  ");
        tmp.add("　(`Д´ヽ  ");
        tmp.add("ヽ( `Д´)ノ");
        animel.add((ArrayList<String>) tmp.clone());
        tmp=new ArrayList<>();
        tmp.add("(*ﾟдﾟ)＜ｶｲﾊﾞｰ");
        tmp.add("(*ﾟдﾟ)       ");
        tmp.add("(*ﾟдﾟ)       ");
        tmp.add("(*ﾟдﾟ)       ");
        animel.add((ArrayList<String>) tmp.clone());
        tmp=new ArrayList<>();
        tmp.add("｜ω・)");
        tmp.add("｜     ");
        tmp.add("｜     ");
        tmp.add("｜     ");
        animel.add((ArrayList<String>) tmp.clone());
        tmp=new ArrayList<>();
        tmp.add("Y");
        tmp.add("A");
        tmp.add("T");
        tmp.add("T");
        tmp.add("A");
        tmp.add("!");
        animel.add((ArrayList<String>) tmp.clone());
    }

    private static long animetime = System.currentTimeMillis();
    private static ArrayList<String> animeList = new ArrayList<>();

    private static void showCalender() throws InterruptedException {
        if (busy) {
            anime = 0;
            if (animel.size() == 0) {
                setupAnime();
            } else if (animel.size() == 1) {
                animeList = animel.get(0);
            } else {
                animeList = animel.get(new Random().nextInt(animel.size() - 1));
            }
            animetime = System.currentTimeMillis() - 1000;
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String s = "";
        if (colon) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            s = formatter.format(zonedDateTime);
            colon = false;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH mm");
            s = formatter.format(zonedDateTime);
            colon = true;
        }
        s = s + " " + animeList.get(anime);
        anime++;
        if (anime == animeList.size()) {
            anime = 0;
        }
        Thread thread = new Thread(new OneWindowWriter(makeybuffer(s), false));
        if (animetime + 1000 - System.currentTimeMillis() > 0) {
            Thread.sleep(animetime + 1000 - System.currentTimeMillis());
        }
        writing = thread;
        writing.start();
        animetime = System.currentTimeMillis();
    }

    public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
        HashSet<CommPortIdentifier> h = new HashSet<>();
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
            switch (com.getPortType()) {
                case CommPortIdentifier.PORT_SERIAL:
                    try {
                        CommPort thePort = com.open("CommUtil", 50);
                        thePort.close();
                        h.add(com);
                    } catch (PortInUseException e) {
                        System.out.println("Port, " + com.getName() + ", is in use.");
                    } catch (Exception e) {
                        System.err.println("Failed to open port " + com.getName());
                        e.printStackTrace();
                    }
            }
        }
        return h;
    }
}
