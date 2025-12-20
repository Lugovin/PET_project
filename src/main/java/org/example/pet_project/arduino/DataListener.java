package org.example.pet_project.arduino;

// Интерфейс для получения данных
public interface DataListener {
    void onDataReceived(String data);

    void onError(String error);

    void onConnectionStatusChanged(boolean connected);
}
