package gui.controllers.tables;

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
            return;
        }
        try {
            // Expecting format: 2025-11-12 09:00:00.0
            // Split date and time
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

    public boolean isEmpty() {
        return datePicker.getValue() == null;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TextField getTimeField() {
        return timeField;
    }
}