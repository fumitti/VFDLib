package com.fumitti.vfdlib.Reader;

import com.fumitti.vfdlib.VFDNewsPanel;
import org.apache.http.client.config.RequestConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

/**
 * Created by fumitti on 2015/05/21.
 */
public class gooTrainInfo extends TimerTask implements ReaderInterface {
    private HashMap<String, infomation> infomap = new HashMap<>();
    private String beforetime = "";

    public gooTrainInfo() {
        TimerTask task = this;
        Timer timer = new Timer("gooTrainInfo定期取得");
        timer.schedule(task, 1000 * 120, 120 * 1000);
        System.out.println("TrainInformation Init");
    }

    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect("http://transit.goo.ne.jp/unkou/train/kantou/").timeout(0).get();
            String time = doc.getElementsByClass("update02").text();
            if (!time.equals(beforetime)) {
                getinfo();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getinfo() {
        boolean pf = true;
        boolean qf = true;
        // request configuration
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        // create client

        try {
            Document doc = Jsoup.connect("http://transit.goo.ne.jp/unkou/train/kantou/").timeout(0).get();
            String time = doc.getElementsByClass("update02").text();
            beforetime = time;
            System.out.println("----運行情報 " + time + "----");
            time = "【関東鉄道運行状況 " + time + " 現在】";
            infomap.clear();
            for (Element traininfo : doc.getElementsByClass("traininfo").first().getElementsByTag("li")) {
                infomation work = new infomation();
                work.setTime(traininfo.getElementsByClass("time").text());
                work.setLine(traininfo.getElementsByClass("name").text());
                work.setStatus(traininfo.getElementsByClass("status").text());
                work.setDesc(traininfo.getElementsByClass("description").text());
                if (!infomap.containsKey(work.getLine()))
                    infomap.put(work.getLine(), work);
            }
            ArrayList<Map.Entry<String, infomation>> entries = new ArrayList(infomap.entrySet());
            Collections.sort(entries, (obj1, obj2) -> {
                        int i1, i2;
                        switch (obj1.getValue().getStatus()) {
                            case "運転見合わせ":
                                i1 = 0;
                                break;
                            case "運転再開":
                                i1 = 1;
                                break;
                            case "列車遅延":
                                i1 = 2;
                                break;
                            case "運転状況":
                                i1 = 8;
                                break;
                            case "平常運転":
                                i1 = 9;
                                break;
                            default:
                                i1 = 5;
                                break;
                        }
                        switch (obj2.getValue().getStatus()) {
                            case "運転見合わせ":
                                i2 = 0;
                                break;
                            case "運転再開":
                                i2 = 1;
                                break;
                            case "列車遅延":
                                i2 = 2;
                                break;
                            case "運転状況":
                                i2 = 8;
                                break;
                            case "平常運転":
                                i2 = 9;
                                break;
                            default:
                                i2 = 5;
                                break;
                        }
                        return i1 - i2;
                    }
            );

            Collections.sort(entries, (obj1, obj2) -> {
                        if (obj1.getValue().getStatus().equals(obj2.getValue().getStatus())) {
                            return 0;
                        }
                        String[] obj1time = obj1.getValue().getTime().split(":");
                        String[] obj2time = obj2.getValue().getTime().split(":");
                        if (Integer.parseInt(obj1time[0]) < 4) {
                            obj1time[0] = String.valueOf(Integer.parseInt(obj1time[0]) + 24);
                        }
                        if (Integer.parseInt(obj1time[1]) < 4) {
                            obj1time[1] = String.valueOf(Integer.parseInt(obj1time[1]) + 24);
                        }
                        if (obj1time[0] == obj2time[0]) {
                            return Integer.parseInt(obj1time[1]) - Integer.parseInt(obj2time[1]);
                        } else {
                            return Integer.parseInt(obj2time[0]) - Integer.parseInt(obj1time[0]);
                        }
                    }
            );
            ArrayList<String> workList = new ArrayList<>();
            for (Map.Entry<String, infomation> ent : entries) {
                infomation infomation = ent.getValue();
                String outText = "【" + infomation.getLine() + " " + infomation.getTime() + " " + infomation.getStatus() + "】" + infomation.getDesc();
                if (infomation.isCriticalLine()) {
                    if (pf) {
                        workList.add(time);
                        pf = false;
                    }
                    workList.add(outText);
                } else {
                    if (qf) {
                        workList.add(time);
                        qf = false;
                    }
                    workList.add(outText);
                }
                System.out.println(outText);
            }
            if (qf && pf) {
                workList.add(time + " 現在、各線平常通り運転しています。");
            }
            VFDNewsPanel.Que.put(this,workList);
        } catch (Exception e) {
            e.printStackTrace();
            VFDNewsPanel.newsQue.add("現在、運行情報の取得が出来ません。");
        }
    }
}

class infomation {
    private String time;
    private String line;
    private String status;
    private String desc;

    public String getTime() {
        return time;
    }

    public String getLine() {
        return line;
    }

    public String getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isCriticalLine() {
        if (line == null || status == null) {
            return false;
        } else if (line.contains("伊勢崎")) {
        } else if (line.contains("日比谷")) {
        } else if (line.contains("スカイツリー")) {
        } else if (line.contains("常磐")) {
        } else if (line.contains("千代田")) {
        } else if (line.contains("半蔵門")) {
        } else if (line.contains("つくば")) {
        } else {
            return false;
        }
        if (status.contains("平常運転")) {
        } else if (status.contains("運転状況")) {
        } else {
            return true;
        }
        return false;
    }
}
