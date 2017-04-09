package maria.ana.matei.com.publictransportationapp;

public class Station {
    private String numeStatie;
    private double latitudine;
    private double lobgitudine;

    public Station(String numeStatie, double latitudine, double lobgitudine) {
        this.numeStatie = numeStatie;
        this.latitudine = latitudine;
        this.lobgitudine = lobgitudine;
    }

    public String getNumeStatie() {
        return numeStatie;
    }

    public void setNumeStatie(String numeStatie) {
        this.numeStatie = numeStatie;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLobgitudine() {
        return lobgitudine;
    }

    public void setLobgitudine(double lobgitudine) {
        this.lobgitudine = lobgitudine;
    }

    @Override
    public String toString() {
        return getNumeStatie() + " " + getLatitudine() + " " + getLobgitudine();
    }
}
