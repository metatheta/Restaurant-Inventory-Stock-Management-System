package gui.controllers.tables;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimePicker extends HBox {
    private final DatePicker datePicker;
    private final TextField timeField;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DateTimePicker() {
        this.setSpacing(10);

        datePicker = new DatePicker();
        datePicker.setPromptText("yyyy-MM-dd");

        timeField = new TextField();
        timeField.setPromptText("HH:mm:ss");
        timeField.setPrefWidth(100);

        this.getChildren().addAll(datePicker, timeField);
    }

    public void setTimestamp(String timestamp) {
        if (timestamp == null || timestamp.equals("NULL") || timestamp.isEmpty()) {
            datePicker.setValue(null);
            timeField.setText("");
            return;
        }
        try {
            String[] parts = timestamp.split(" ");
            datePicker.setValue(LocalDate.parse(parts[0]));

            if (parts.length > 1) {
                String timePart = parts[1];
                if (timePart.contains(".")) {
                    timePart = timePart.split("\\.")[0];
                }
                timeField.setText(timePart);
            } else {
                timeField.setText("00:00:00");
            }
        } catch (Exception e) {
            System.err.println("Error parsing timestamp: " + timestamp);
        }
    }

    public void setCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        datePicker.setValue(now.toLocalDate());
        timeField.setText(now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public String getTimestamp() {
        LocalDate date = datePicker.getValue();
        String time = timeField.getText();

        if (date == null) return null;

        if (time == null || time.isBlank()) {
            time = "00:00:00";
        }

        try {
            return LocalDateTime.of(date, LocalTime.parse(time)).format(formatter);
        } catch (Exception e) {
            return date.toString() + " " + time;
        }
    }

    public LocalDateTime getDateTimeValue() {
        LocalDate d = datePicker.getValue();
        if (d == null) return null;
        String t = timeField.getText();
        if (t == null || t.isBlank()) t = "00:00:00";
        try {
            return LocalDateTime.of(d, LocalTime.parse(t));
        } catch (Exception e) {
            return d.atStartOfDay();
        }
    }

    public boolean isEmpty() {
        return datePicker.getValue() == null;
    }

    public ObjectProperty<LocalDate> getDateProperty() {
        return datePicker.valueProperty();
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TextField getTimeField() {
        return timeField;
    }
}