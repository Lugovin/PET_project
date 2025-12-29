package org.example.pet_project.mqtt;


import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignSensorErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            // Ваше исключение для отсутствующего датчика
            return new SensorNotFoundException("Датчик с таким ID не найден. Или данных нет.");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
