package org.example.eventdrivenprogrammingproject1;

import java.io.*;
import java.net.*;
import java.util.*;


public class LectureSchedulerServer {
    private static final int PORT = 291;
    private static final Map<String, List<Lecture>> schedule = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Received: " + request);
                    handleRequest(request);
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void handleRequest(String request) {
            String[] parts = request.split("; ");
            String action = parts[0];

            try {
                switch (action) {
                    case "Add Lecture":
                        addLecture(parts);
                        break;
                    case "Remove Lecture":
                        removeLecture(parts);
                        break;
                    case "Display Schedule":
                        displaySchedule(parts);
                        break;
                    default:
                        throw new IncorrectActionException("Invalid action: " + action);
                }
            } catch (IncorrectActionException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void addLecture(String[] parts) {
            if (parts.length != 6) {
                out.println("Invalid Add Lecture request format.");
                return;
            }

            String module = parts[1];
            String date = parts[2];
            String startTime = parts[3];
            String endTime = parts[4];
            String room = parts[5];

            Lecture newLecture = new Lecture(module, date, startTime, endTime, room);
            List<Lecture> lectures = schedule.computeIfAbsent(module, k -> new ArrayList<>());

            if (isClash(newLecture)) {
                out.println("Scheduling clash detected.");
            } else {
                lectures.add(newLecture);
                out.println("Lecture added successfully.");
            }
        }

        private boolean isClash(Lecture newLecture) {
            for (List<Lecture> lectures : schedule.values()) {
                for (Lecture lecture : lectures) {
                    if (lecture.clashesWith(newLecture)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void removeLecture(String[] parts) {
            if (parts.length != 6) {
                out.println("Invalid Remove Lecture request format.");
                return;
            }

            String module = parts[1];
            String date = parts[2];
            String startTime = parts[3];
            String endTime = parts[4];
            String room = parts[5];

            Lecture lectureToRemove = new Lecture(module, date, startTime, endTime, room);
            List<Lecture> lectures = schedule.get(module);

            if (lectures != null) {
                boolean removed = false;
                for (Lecture lecture : lectures) {
                    System.out.println("Checking lecture: " + lecture);
                    if (lecture.equals(lectureToRemove)) {
                        removed = true;
                        break;
                    }
                }

                if (lectures.removeIf(lecture -> lecture.equals(lectureToRemove))) {
                    out.println("Lecture removed. Freed: " + room + " on " + date + " from " + startTime + " to " + endTime);
                } else {
                    out.println("Lecture not found.");
                }
            } else {
                out.println("Lecture not found.");
            }

        }

        private void displaySchedule(String[] parts) {
            if (parts.length != 2) {
                out.println("Invalid Display Schedule request format.");
                return;
            }

            String module = parts[1];
            List<Lecture> lectures = schedule.get(module);

            if (lectures == null || lectures.isEmpty()) {
                out.println("No lectures scheduled for " + module);
            } else {
                for (Lecture lecture : lectures) {
                    out.println(lecture);
                }
            }
        }
    }
}
