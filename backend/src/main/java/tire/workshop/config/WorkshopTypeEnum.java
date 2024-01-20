package tire.workshop.config;

public enum WorkshopTypeEnum {
    JSON("JSON"),
    XML("XML");

    public final String label;

    private WorkshopTypeEnum(String label) {
        this.label = label;
    }
}
