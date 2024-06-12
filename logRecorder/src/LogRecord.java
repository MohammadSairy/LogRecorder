import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class LogRecord implements Serializable, Comparable<LogRecord> {
    private Date timestamp;
    private long timestamp2;
    private String ip;
    private String username;
    private String role;
    private String url;
    private String description;




    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");

    public LogRecord(String date, String time,long timestamp, String ip, String username, String role, String url, String description) throws ParseException {
        this.timestamp = dateFormat.parse(date + "-" + time);
        this.timestamp2=timestamp;
        this.ip = ip;
        this.username = username;
        this.role = role;
        this.url = url;
        this.description = description;
    }





    public Date getTimestamp() {
        return timestamp;
    }
    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        return formatter.format(timestamp);
    }

    public String getIp() {
        return ip;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public int compareTo(LogRecord other) {
        return this.timestamp.compareTo(other.timestamp);
    }


    public static ArrayList<LogRecord> readLogRecords(String filePath) {
        ArrayList<LogRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("-");
                if (parts.length >= 5) {
                    long temp=Long.parseLong(parts[2]);
                    LogRecord record = new LogRecord(parts[0], parts[1], temp, parts[3], parts[4], parts[5],parts[6], parts.length > 7 ? parts[7] : null);
                    records.add(record);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        Collections.sort(records);
        return records;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "timestamp=" + timestamp +
                ", ip='" + ip + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}
