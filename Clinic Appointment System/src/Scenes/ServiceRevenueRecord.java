package Controller;

public class ServiceRevenueRecord {

    private String year;
    private String month;
    private String service;
    private double revenue;

    public ServiceRevenueRecord(String year, String month, String service, double revenue) {
        this.year = year;
        this.month = month;
        this.service = service;
        this.revenue = revenue;
    }

    public String getYear() { return year; }
    public String getMonth() { return month; }
    public String getService() { return service; }
    public double getRevenue() { return revenue; }

    public void setYear(String year) { this.year = year; }
    public void setMonth(String month) { this.month = month; }
    public void setService(String service) { this.service = service; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

}
