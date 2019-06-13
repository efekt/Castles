package it.efekt.mc.castles;

public enum GameState {
    LOBBY(2700),
    PREPARATION(2700),
    PEACE(2700),
    WAR(2700),
    FINISHED(0);

    private long time;

    GameState(long time){
        this.time = time;
    }

    public long getLength(){
        Config config = CastlesPlugin.castlesManager.getInstance().getConfig();
        switch (this){
            case PEACE: return config.getPeaceTime();
            case WAR: return config.getWarTime();
            default:
                return this.time;
        }
    }

}

