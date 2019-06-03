package it.efekt.mc.castles;

public enum GameState {
    LOBBY(20),
    PREPARATION(20),
    PEACE(20),
    WAR(20),
    FINISHED(0);

    private long time;

    GameState(long time){
        this.time = time;
    }

    public long getLength(){
        return this.time;
    }

}

