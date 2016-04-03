package org.self.example;

/**
 * Created by abhi on 3/4/16.
 */
public class UserData {
    String name,info;
    public UserData(String name,String Info){
        this.name=name;
        this.info=Info;
    }
    public String getName(){
        return name;
    }
    public String getInfo(){
        return info;
    }
    public void setName(String Name){
        name=Name;
    }
    public void setInfo(String info){
        this.info=info;
    }

}
