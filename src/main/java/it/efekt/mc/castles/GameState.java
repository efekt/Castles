package it.efekt.mc.castles;

public enum GameState {
    LOBBY(0),
    PREPARATION(5),
    PEACE(10),
    WAR(10),
    FINISHED(0);

    private long time;

    GameState(long time){
        this.time = time;
    }

    public long getLength(){
        return this.time;
    }

}

