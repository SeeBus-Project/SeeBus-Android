package com.opensource.seebus.history;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper{
    // History: 최근 기록 테이블, Favorite: 즐겨찾기 테이블
    // 테이블에 저장하는 데이터 (id, busNm, departureNo, departureNm, destinationNo, destinationNm)
    // --> (id, 버스 이름(번호), 출발 정류소 번호, 출발 정류소 이름, 도착 정류소 번호, 도착 정류소 이름)

    private static final int DB_VERSION = 3; // 버전 3으로 업데이트
    private static final String DB_NAME = "history.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 데이터베이스 -> 테이블 -> 컬럼 -> 값
        // history.db --> 데이터베이스
        // History, Favorite --> 테이블
        // id, busNm, departureNo, departureNm, destinationNo, destinationNm --> 컬럼
        db.execSQL("CREATE TABLE IF NOT EXISTS History (id INTEGER PRIMARY KEY AUTOINCREMENT, busNm TEXT NOT NULL, departureNo TEXT NOT NULL, departureNm TEXT NOT NULL, destinationNo TEXT NOT NULL, destinationNm TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Favorite (id INTEGER PRIMARY KEY AUTOINCREMENT, busNm TEXT NOT NULL, departureNo TEXT NOT NULL, departureNm TEXT NOT NULL, destinationNo TEXT NOT NULL, destinationNm TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS History");
        db.execSQL("DROP TABLE IF EXISTS Favorite");
        onCreate(db);
    }

    // SELECT 문 - 전체 조회
    // 최근 기록 조회
    public ArrayList<HistoryItem> getHistory() {
        ArrayList<HistoryItem> historyItems = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        // History 테이블에서 데이터를 모두(*) 가져온다(SELECT). id를 내림차순(DESC) 정렬해서(ORDER BY).
        Cursor cursor = db.rawQuery("SELECT * FROM History ORDER BY id DESC", null);

        if(cursor.getCount() != 0) {
            // 조회할 데이터가 있을 때 내부 수정
            while (cursor.moveToNext()) { // 다음 데이터가 있을 때까지
                // gerColumnIndex("컬럼명")
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String busNm = cursor.getString(cursor.getColumnIndex("busNm"));
                String departureNo = cursor.getString(cursor.getColumnIndex("departureNo"));
                String departureNm = cursor.getString(cursor.getColumnIndex("departureNm"));
                String destinationNo = cursor.getString(cursor.getColumnIndex("destinationNo"));
                String destinationNm = cursor.getString(cursor.getColumnIndex("destinationNm"));

                HistoryItem historyItem = new HistoryItem();

                historyItem.setId(id);
                historyItem.setBusNm(busNm);
                historyItem.setDepartureNo(departureNo);
                historyItem.setDepartureNm(departureNm);
                historyItem.setDestinationNo(destinationNo);
                historyItem.setDestinationNm(destinationNm);

                historyItems.add(historyItem);
            }
        }
        cursor.close();

        return historyItems;
    }

    // 즐겨찾기 조회
    public ArrayList<HistoryItem> getFavorite() {
        ArrayList<HistoryItem> favoriteItems = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        // History 테이블에서 데이터를 모두(*) 가져온다(SELECT). id를 내림차순(DESC) 정렬해서(ORDER BY).
        Cursor cursor = db.rawQuery("SELECT * FROM Favorite ORDER BY id DESC", null);

        if(cursor.getCount() != 0) {
            // 조회할 데이터가 있을 때 내부 수정
            while (cursor.moveToNext()) { // 다음 데이터가 있을 때까지
                // gerColumnIndex("컬럼명")
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String busNm = cursor.getString(cursor.getColumnIndex("busNm"));
                String departureNo = cursor.getString(cursor.getColumnIndex("departureNo"));
                String departureNm = cursor.getString(cursor.getColumnIndex("departureNm"));
                String destinationNo = cursor.getString(cursor.getColumnIndex("destinationNo"));
                String destinationNm = cursor.getString(cursor.getColumnIndex("destinationNm"));

                HistoryItem favoriteItem = new HistoryItem();

                favoriteItem.setId(id);
                favoriteItem.setBusNm(busNm);
                favoriteItem.setDepartureNo(departureNo);
                favoriteItem.setDepartureNm(departureNm);
                favoriteItem.setDestinationNo(destinationNo);
                favoriteItem.setDestinationNm(destinationNm);

                favoriteItems.add(favoriteItem);
            }
        }
        cursor.close();

        return favoriteItems;
    }

    // INSERT 문
    // 최근기록 삽입
    public void insertHistory(String _busNm, String _departureNo, String _departureNm, String _destinationNo, String _destinationNm) { // id는 AUTOINCREMENT로 자동으로 넣어주니까 Insert에서 넣어둘 필요X
        ArrayList<HistoryItem> historyItems = new ArrayList<>();
        historyItems = getHistory();

        // 개수 검사 - 5개
        if (historyItems.size() == 5) {
            // 개수가 5개가 되면 가장 오래된 기록 삭제
            deleteHistory(historyItems.get(4).getId());
        }

        // 중복 검사
        for (int i = 0; i < historyItems.size(); i++) {
            HistoryItem historyItem = historyItems.get(i);

            if (_busNm.equals(historyItem.getBusNm()) && _departureNm.equals(historyItem.getDepartureNm()) && _destinationNm.equals(historyItem.getDestinationNm())) {
                // 중복 데이터가 존재하면 db에 있는 중복 데이터 삭제하기
                deleteHistory(historyItem.getId());
                break;
            }
        }

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO History (busNm, departureNo, departureNm, destinationNo, destinationNm) VALUES('" + _busNm + "', '" + _departureNo + "', '" + _departureNm + "', '" + _destinationNo + "', '" + _destinationNm + "');");
        db.close();
    }

    // 즐겨찾기 삽입
    public void insertFavorite(String _busNm, String _departureNo, String _departureNm, String _destinationNo, String _destinationNm) { // id는 AUTOINCREMENT로 자동으로 넣어주니까 Insert에서 넣어둘 필요X
        // 중복 검사
        boolean isNotDuplicate = true; // 중복 여부 (중복되지 않는다. - true)
        ArrayList<HistoryItem> favoriteItems = new ArrayList<>();
        favoriteItems = getFavorite();

        for (int i = 0; i < favoriteItems.size(); i++) {
            HistoryItem favoriteItem = favoriteItems.get(i);

            if (_busNm.equals(favoriteItem.getBusNm()) && _departureNm.equals(favoriteItem.getDepartureNm()) && _destinationNm.equals(favoriteItem.getDestinationNm())) {
                // 중복 데이터가 존재하면 db에 해당 데이터 삽입하지 않기
                isNotDuplicate = false;
                break;
            }
        }

        if (isNotDuplicate) { // 중복 데이터가 존재하지 않으면 db에 해당 데이터 삽입하기
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Favorite (busNm, departureNo, departureNm, destinationNo, destinationNm) VALUES('" + _busNm + "', '" + _departureNo + "', '" + _departureNm + "', '" + _destinationNo + "', '" + _destinationNm + "');");
        db.close();
        }
    }

    // DELETE 문
    // 최근기록 삭제
    public void deleteHistory(int _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM History WHERE id='" + _id + "'");
        db.close();
    }

    // 즐겨찾기 삭제
    public void deleteFavorite(int _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Favorite WHERE id='" + _id + "'");
        db.close();
    }
}
