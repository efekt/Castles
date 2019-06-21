package it.efekt.mc.castles;

public enum GameState {
    LOBBY(2700, "LOBBY"),
    PREPARATION(2700, "PRZYGOTOWANIA"),
    PEACE(2700, "Czas Pokoju"),
    WAR(2700, "Czas Wojny"),
    FINISHED(0, "KONIEC");

    private long time;
    private String translated;

    GameState(long time, String translated){
        this.time = time;
        this.translated = translated;
    }

    public String getTranslated() {
        return translated;
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

