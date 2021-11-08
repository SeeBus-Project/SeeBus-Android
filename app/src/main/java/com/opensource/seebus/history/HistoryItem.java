package com.opensource.seebus.history;

public class HistoryItem {
    private int id;                 // 히스토리의 고유 ID
    private String busNm;           // 버스 이름(번호)
    private String busRouteId;     // 버스 노선 번호
    //private String departureNo;     // 출발 정류장 번호
    private String departureNm;     // 출발 정류장 이름
    //private String destinationNo;   // 도착 정류장 번호
    private String destinationNm;   // 도착 정류장 이름

    public HistoryItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBusNm() { return busNm; }

    public void setBusNm(String busNm) { this.busNm = busNm; }

    public String getBusRouteId() {
        return busRouteId;
    }

    public void setBusRouteId(String busRouteId) {
        this.busRouteId = busRouteId;
    }

 //   public String getDepartureNo() {
    //    return departureNo;
    // }

    //public void setDepartureNo(String departureNo) {
    //    this.departureNo = departureNo;
    //}

    public String getDepartureNm() {
        return departureNm;
    }

    public void setDepartureNm(String departureNm) {
        this.departureNm = departureNm;
    }

    // public String getDestinationNo() {
    //     return destinationNo;
    //}

    // public void setDestinationNo(String destinationNo) {
    //     this.destinationNo = destinationNo;
    //}

    public String getDestinationNm() {
        return destinationNm;
    }

    public void setDestinationNm(String destinationNm) {
        this.destinationNm = destinationNm;
    }
}
