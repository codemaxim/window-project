package com.ehdbsrhktmddn.senierproject;


import android.content.Intent;
import android.util.Log;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FineDustMeasure {

    private static HashMap<String, String> regionMap;

    static{
        regionMap = new HashMap<String, String>();
        regionMap.put("충청북도", "충북");
        regionMap.put("서울특별시", "서울");
    }
    public class FineDustVO {
        String region;
        String dateTime;
        String pm10;
        String pm25;

        public FineDustVO(String region, String dateTime, String pm10, String pm25) {
            // TODO Auto-generated constructor stub
            this.region = region;
            this.dateTime = dateTime;
            this.pm10 = pm10;
            this.pm25 = pm25;
        }

    }

    private static FineDustMeasure fm = new FineDustMeasure();

    private FineDustMeasure() {

    }

    public static FineDustMeasure getInstance() {
        return fm;
    }

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    public FineDustVO GetFineDust_pm(String[] region) {

        try {
            Document rs_xml = XmlParser.convertStringToXMLDocument(requestURL(region[0]));
            NodeList nList = rs_xml.getElementsByTagName("item");
            System.out.println("Node length : " + nList.getLength());
            for(int temp = 0 ; temp < nList.getLength() ; temp++) {

                Node node = nList.item(temp);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    if(getTagValue("stationName", e).equals(region[1]) || temp == nList.getLength() - 1) {
                        //해당 동/읍이 관측이 안되면 맨 마지막 지역의 정보를 반환
                        return new FineDustVO(getTagValue("stationName", e),
                                getTagValue("dataTime", e),
                                getTagValue("pm10Value", e),
                                getTagValue("pm25Value", e));
                    }

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String requestURL(String region) throws IOException{ //api에 요청하여 결과 값을 리턴함
        Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA : ", region);
        StringBuilder urlBuilder = new StringBuilder("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=0e5b%2B1WHzmQXFa2iFaK4Rqs%2B8dz%2B08T3L8Q4Ko25dVLtS9NxbQGIR538RBMZI5Dob5Z8HB4DrJw0LmWk98Yckg%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("sidoName","UTF-8") + "=" + URLEncoder.encode(region, "UTF-8")); /*시도 이름 (서울, 부산, 대구, 인천, 광주, 대전, 울산, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 세종)*/
        urlBuilder.append("&" + URLEncoder.encode("ver","UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8")); /*버전별 상세 결과 참고문서 참조*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;

        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
//        System.out.println(sb.toString());
        return sb.toString(); //xml 형태로 리턴
    }

    public static String[] getRealRegion(String region) {

        String region_list[] = region.split(" "); //대한민국 충청북도 청주시 흥덕구 사창동 450
        String region_rs[] = new String[2]; //충청북도, 오창읍

        if(region_list[0].equals("대한민국")) {

            String s = regionMap.get(region_list[1]);
            region_rs[0] = s;
            for(int x = region_list.length - 1; x >= 0 ; x--) {

                if(region_list[x].contains("동") || region_list[x].contains("읍")) {
                    region_rs[1] = region_list[x];
                    return region_rs;
                }
            }
        }

        return null;
    }

//    public static void main(String[] args) throws IOException {
//        FineDustMeasure fm = FineDustMeasure.getInstance();
//        String region = "대한민국 충청북도 청주시 흥덕구 사창동 450";
//        String temp[] = FineDustMeasure.getRealRegion(region);
//
//        FineDustVO vo = fm.GetFineDust_pm(temp);
//        System.out.println("지역 : " + vo.region);
//        System.out.println("시간 : " + vo.dateTime);
//        System.out.println("pm10 : " + vo.pm10);
//        System.out.println("pm25 : " + vo.pm25);
//        System.out.println();
//
//    }
}
