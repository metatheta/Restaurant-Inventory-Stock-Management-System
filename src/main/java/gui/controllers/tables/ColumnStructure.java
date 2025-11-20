package gui.controllers.tables;

public record ColumnStructure(String name, String type, boolean isHidden, boolean isEditable) {
    public ColumnStructure(String name, String type, boolean isHidden) {
        this(name, type, isHidden, false);
    }
}
