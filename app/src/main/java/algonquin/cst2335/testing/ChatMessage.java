package algonquin.cst2335.testing;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name="Name")
    private String name;

    @ColumnInfo(name="Date")
    private String date;

    @ColumnInfo(name="Url")
    private String url;

    public ChatMessage(){}
    public ChatMessage(String name, String date, String url){

        this.name = name;
        this.date = date;
        this.url = url;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getUrl(){
        return url;
    }

    public void setName(String name ){
        this.name = name;
    }

    public void setDate(String date ){
        this.date = date;
    }

    public void setUrl(String url ){
        this.url = url;
    }
}
