package app.vercel.lucasgabrielcosta.mindra.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.vercel.lucasgabrielcosta.mindra.database.Converters;

@Entity(tableName = "notes")
@TypeConverters(Converters.class)
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String category;
    private Date creationDate;
    private List<String> connections;

    public Note() {
        this.creationDate = new Date();
        this.connections = new ArrayList<>();
    }

    @Ignore
    public Note(int id, String title, String content, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.creationDate = new Date();
        this.connections = extractConnections(content);
    }

    private List<String> extractConnections(String content) {
        Set<String> uniqueConnections = new HashSet<>();

        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            uniqueConnections.add(matcher.group(1));
        }

        return new ArrayList<>(uniqueConnections);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.connections = extractConnections(content);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    public int getConnectionCount() {
        return connections != null ? connections.size() : 0;
    }

    public String getFormattedDate() {
        return creationDate != null ? creationDate.toString() : "";
    }

    public String getContentPreview(int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}