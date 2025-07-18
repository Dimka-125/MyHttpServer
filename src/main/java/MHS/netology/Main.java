package MHS.netology;


import java.io.*;
import java.net.ServerSocket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Server started on port 9999");
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {
                    // Читаем первую строку запроса
                    final var requestLine = in.readLine();

                    // Парсим запрос
                    Request request = new Request(requestLine);

                    // Получаем путь из запроса
                    String path = request.getPath();

                    // Проверяем, что путь допустим
                    if (path == null || !validPaths.contains(path)) {
                        writeResponse(out, "HTTP/1.1 404 Not Found", "", null);
                        continue;
                    }

                    // Формируем путь к файлу
                    final var filePath = Path.of(".", "public", path);
                    if (!Files.exists(filePath)) {
                        writeResponse(out, "HTTP/1.1 404 Not Found", "",null);
                        continue;
                    }

                    final var mimeType = Files.probeContentType(filePath);

                    // Обработка /classic.html с заменой {time}
                    if (path.equals("/classic.html")) {
                        String time = request.getQueryParam("time").orElse(LocalDateTime.now().toString());

                        String template = Files.readString(filePath);
                        String content = template.replace("{time}", time);
                        byte[] contentBytes = content.getBytes();

                        writeResponse(
                                out,
                                "HTTP/1.1 200 OK",
                                "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + contentBytes.length + "\r\n" +
                                        "Connection: close",
                                contentBytes
                        );
                        continue;
                    }

                    // Обычный случай: отправляем файл как есть
                    long length = Files.size(filePath);
                    writeResponse(
                            out,
                            "HTTP/1.1 200 OK",
                            "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close",
                            null
                    );
                    Files.copy(filePath, out);
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Вспомогательный метод для отправки ответа
    private static void writeResponse(BufferedOutputStream out, String status, String headers, byte[] body) throws IOException {
        out.write((status + "\r\n" + headers + "\r\n\r\n").getBytes());
        if (body != null) {
            out.write(body);
        }
        out.flush();
    }
}