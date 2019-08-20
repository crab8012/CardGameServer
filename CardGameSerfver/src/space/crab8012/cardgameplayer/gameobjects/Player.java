package space.crab8012.cardgameplayer.gameobjects;

import java.io.Serializable;

public class Player implements Serializable {
    private String name = "";
    private String icon = "";
    int score = 0;

    public Player(String name, String icon){
        this.name = name;
        this.icon = icon;
    }
    public void setScore(int newScore){
        score = newScore;
    }
    public void addScore(int add){
        score += add;
    }
    public void removeScore(int remove){
        score -= remove;
        if(score < 0){
            score = 0;
        }
    }
    public int getScore(){
        return score;
    }
    public String getName(){
        return name;
    }
    public String getIcon(){
        return icon;
    }
}
