package com.opensource.seebus.history;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper{
    // 현재 DB 테이블 (id, busNm, busRouteId, departureNm, destinationNm)
    // --> (id, 버스 이름(번호), 버스 노선 id, 출발 정류소 이름, 도착 정류소 이름)

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "history.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 데이터 베이스가 생성이 될 때 호출
        // 데이터베이스 -> 테이블 -> 컬럼 -> 값
        // TEXT: String이라고 생각, NOT NULL: 여기에는 데이터가 반드시 있어야 해.
        // history.db --> 데이터베이스
        // History --> 테이블
        // id, busRouteId, departureNo, departureNm, destinationNo, destinationNm --> 컬럼에 속함.
        //db.execSQL("CREATE TABLE IF NOT EXISTS History (id INTEGER PRIMARY KEY AUTOINCREMENT, busNm TEXT NOT NULL, busRouteId TEXT NOT NULL, departureNo TEXT NOT NULL, departureNm TEXT NOT NULL, destinationNo TEXT NOT NULL, destinationNm TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS History (id INTEGER PRIMARY KEY AUTOINCREMENT, busNm TEXT NOT NULL, busRouteId TEXT NOT NULL, departureNm TEXT NOT NULL, destinationNm TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS History");
        onCreate(db);
    }

    // SELECT 문 - 전체 조회
    public ArrayList<HistoryItem> getHistory() {
        ArrayList<HistoryItem> historyItems = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        // Cursor: 가라키는 것
        // 아래 쿼리문 해석
        // History 테이블에서 데이터를 모두(*) 가져온다(SELECT). id를 내림차순(DESC) 정렬해서(ORDER BY).
        Cursor cursor = db.rawQuery("SELECT * FROM History ORDER BY id DESC", null);

        if(cursor.getCount() != 0) {
            // 조회할 데이터가 있을 때 내부 수정
            while (cursor.moveToNext()) { // 다음 데이터가 있을 때까지
                // gerColumnIndex("컬럼명")
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String busNm = cursor.getString(cursor.getColumnIndex("busNm"));
                String busRouteId = cursor.getString(cursor.getColumnIndex("busRouteId"));
                //String departureNo = cursor.getString(cursor.getColumnIndex("departureNo"));
                String departureNm = cursor.getString(cursor.getColumnIndex("departureNm"));
                //String destinationNo = cursor.getString(cursor.getColumnIndex("destinationNo"));
                String destinationNm = cursor.getString(cursor.getColumnIndex("destinationNm"));

                HistoryItem historyItem = new HistoryItem();

                historyItem.setId(id);
                historyItem.setBusNm(busNm);
                historyItem.setBusRouteId(busRouteId);
                //historyItem.setDepartureNo(departureNo);
                historyItem.setDepartureNm(departureNm);
                //historyItem.setDestinationNo(destinationNo);
                historyItem.setDestinationNm(destinationNm);

                historyItems.add(historyItem);
            }
        }
        cursor.close();

        return historyItems;
    }

    // INSERT 문
    //public void InsertHistory(String _busNm, String _busRouteId, String _departureNo, String _departureNm, String _destinationNo, String _destinationNm) { // id는 AUTOINCREMENT로 자동으로 넣어주니까 Insert에서 넣어둘 필요X
    public void InsertHistory(String _busNm, String _busRouteId, String _departureNm, String _destinationNm) { // id는 AUTOINCREMENT로 자동으로 넣어주니까 Insert에서 넣어둘 필요X
        // 중복 검사
        boolean isNotDuplicate = true; // 중복 여부 (중복되지 않는다. - true)
        ArrayList<HistoryItem> historyItems = new ArrayList<>();
        historyItems = getHistory();

        for (int i = 0; i < historyItems.size(); i++) {
            HistoryItem historyItem = historyItems.get(i);

            if (_busNm.equals(historyItem.getBusNm()) && _departureNm.equals(historyItem.getDepartureNm()) && _destinationNm.equals(historyItem.getDestinationNm())) {
                // 중복 데이터가 존재하면 db에 있는 중복 데이터 삭제하기 - 최근 이력 형식으로 만들 때 (순서가 바뀜.)
                deleteHistory(historyItem.getId());

                // 중복 데이터가 존재하면 db에 해당 데이터 삽입하지 않기 - 즐겨찾기 형식으로 만들 때 (순서가 바뀌지 않음.)
                //isNotDuplicate = false;

                break;
            }
        }

        //if (isNotDuplicate) { // 중복 데이터가 존재하지 않으면 db에 해당 데이터 삽입하기 - 즐겨찾기 형식으로 만들 때 (최근 이력 형식으로 만들 때는 if랑 {, }만 지워주면 됨.)
            SQLiteDatabase db = getWritableDatabase();
            //db.execSQL("INSERT INTO History (busNm, busRouteId, departureNo, departureNm, destinationNo, destinationNm) VALUES('" + _busNm + "', '" + _busRouteId + "', '" + _departureNo + "', '" + _departureNm + "', '" + _destinationNo + "', '" + _destinationNm + "');");
            db.execSQL("INSERT INTO History (busNm, busRouteId, departureNm, destinationNm) VALUES('" + _busNm + "', '" + _busRouteId + "', '" + _departureNm + "', '" + _destinationNm + "');");
            db.close();
        //}
    }

    // UPDATE 문
    //public void updateHistory(int _id, String _busNm, String _busRouteId, String _departureNo, String _departureNm, String _destinationNo, String _destinationNm) {
    public void updateHistory(int _id, String _busNm, String _busRouteId, String _departureNm, String _destinationNm) {
        SQLiteDatabase db = getWritableDatabase();
        // WHERE: if문
        // id로 데이터를 넣을 위치 설정
        //db.execSQL("UPDATE History SET busNm='" + _busNm + "', busRouteId='" + _busRouteId + "', departureNo='" + _departureNo + "', departureNm='" + _departureNm + "', destinationNo='" + _destinationNo + "', destinationNm='" + _destinationNm + "'  WHERE id='" + _id + "'");
        db.execSQL("UPDATE History SET busNm='" + _busNm + "', busRouteId='" + _busRouteId + "', departureNm='" + _departureNm + "', destinationNm='" + _destinationNm + "'  WHERE id='" + _id + "'");
        db.close();
    }

    // DELETE 문
    public void deleteHistory(int _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM History WHERE id='" + _id + "'");
        db.close();
    }
}
