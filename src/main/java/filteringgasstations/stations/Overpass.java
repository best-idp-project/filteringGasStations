package filteringgasstations.stations;

/**
 * Intermediate object used as scheme to parse Overpass station files
 */
public class Overpass {

    public double version;
    public String generator;
    public OverpassGasStation[] elements;
}
