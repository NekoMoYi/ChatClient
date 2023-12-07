module com.nekomoyi.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.nekomoyi.chat to javafx.fxml;
    exports com.nekomoyi.chat;
}