package com.hog2020.ex38xmlpullparsermovie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> items = new ArrayList<>();
    ArrayAdapter adapter;

    //영화진흥위원회 api 사이트에서 발급받은키
    String apikey="f5eef3421c602c6cb7ea224104795888";

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView=findViewById(R.id.listview);
        adapter =new ArrayAdapter(this, android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
    }

    public void clickBtn(View view) {
        //네트워크를 통해서 xml 문서를 읽어오기(Internet Permission 주의)
        //네트워크 작업은 main Thread 가 작업하지 못한다
        //별도의 Thread 에게 네트워크 작업을 수행하도록

        Thread t = new Thread(){
            @Override
            public void run() {

                //이 앱을 실행하는 날짜의 하루전
                Date date=new Date(); //현재 날짜를 가진객체
                date.setTime(date.getTime()-(1000*60*60*24));
                //현재시간을 "YYYYMMDD"이 형태로 문자열로 만들어야함
                //현재시간을 특정 포멧형식모양으로 만들어주는 클래스객체
                SimpleDateFormat SDF= new SimpleDateFormat("yyyyMMdd");

                //API 28버전 디바이스 부터는 http 주소를 사용하려면
                //AndroidManifest.xml 에 http 를 사용한다고 설정해야됨

                String dateStr = SDF.format(date);

                String address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml"
                                +"?key="+apikey
                                +"&targetDt="+dateStr
                                +"&itemPerPage=5";

                //위에서 만든 데이터의 인터넷주소(url)에 접속 객체생성
                try {
                    URL url = new URL(address);
                    InputStream is =url.openStream();
                    //바이트단위로 읽으면 사용하기 불편해서 문자단위로 읽어들이는 문자 스트림 변환
                    InputStreamReader isr= new InputStreamReader(is);//문자 스트림
                    //문자스트림은 한글자씩 글자를 읽어들이기 때문에..isr를 통해 서버 데이터를 모두 읽기
                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                    XmlPullParser xpp =factory.newPullParser();

                    //isr을 통해서 데이터를 읽어 오도록..
                    xpp.setInput(isr);

                    int eventType =xpp.getEventType();

                    //item 1개의 String 데이터
                    StringBuffer buffer= null;

                    while(eventType!= XmlPullParser.END_DOCUMENT){
                        switch (eventType){
                            case XmlPullParser.START_DOCUMENT:
                                //별도의 Thread 는 UI 변경이 불가
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "파싱 시작", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                break;
                                case XmlPullParser.START_TAG:
                                    String tagName = xpp.getName();

                                    if(tagName.equals("dailyBoxOffice")){
                                        buffer= new StringBuffer();

                                    }else if(tagName.equals("rank")){

                                        buffer.append("순위:");
                                        xpp.next();
                                        buffer.append(xpp.getText()+"\n");

                                    }else if(tagName.equals("movieNm")){

                                        buffer.append("제목:");
                                        xpp.next();
                                        buffer.append(xpp.getText()+"\n");

                                    }else if(tagName.equals("openDt")){

                                        buffer.append("개봉일:");
                                        xpp.next();
                                        buffer.append(xpp.getText()+"\n");

                                    }else if (tagName.equals("audiAcc")){

                                        buffer.append("누적관객수:");
                                        xpp.next();
                                        buffer.append(xpp.getText()+"\n");
                                    }

                                    break;

                                    case XmlPullParser.TEXT:
                                        break;

                                        case XmlPullParser.END_TAG:
                                            String tagName2= xpp.getName();
                                            if(tagName2.equals("dailyBoxOffice")){

                                                items.add(buffer.toString());
                                                //화면변경은 별도의 Thread 가 할 수 없다
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                            break;
                        }

                        eventType =xpp.next();
                    }



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();// 자동 run 메소드 발동
    }
}