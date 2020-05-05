package nl.harm27.obswebsocket;

import nl.harm27.obswebsocket.api.events.recording.RecordingStopped;
import nl.harm27.obswebsocket.api.requests.recording.PauseRecording;
import nl.harm27.obswebsocket.api.requests.recording.ResumeRecording;
import nl.harm27.obswebsocket.api.requests.recording.StartRecording;
import nl.harm27.obswebsocket.api.requests.recording.StopRecording;
import nl.harm27.obswebsocket.listener.RecordingEventListener;

import java.util.Arrays;
import java.util.Scanner;

import static nl.harm27.obswebsocket.authentication.AuthenticationResult.*;

public class TestClass {
    private static final Object lockingMonitor = new Object();
    private final OBSWebSocket obsWebSocket;

    private TestClass() {
        obsWebSocket = new OBSWebSocket("localhost", 4444, "test1234");
        obsWebSocket.registerListener(new RecordingEventListener() {
            @Override
            public void recordingStopped(RecordingStopped recordingStopped) {
                System.out.println("Recording Stopped");
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        TestClass testClass = new TestClass();
        synchronized (lockingMonitor) {
            while (!testClass.isAuthenticationReady())
                lockingMonitor.wait(200);
        }

        if (testClass.isAuthenticationSuccess())
            testClass.enable();

        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private boolean isAuthenticationSuccess() {
        return AUTHENTICATION_SUCCESS.equals(obsWebSocket.getAuthenticationResult());
    }

    private boolean isAuthenticationReady() {
        return Arrays.asList(AUTHENTICATION_NOT_REQUIRED, AUTHENTICATION_FAILED, AUTHENTICATION_PASSWORD_MISSING,
                AUTHENTICATION_SUCCESS).contains(obsWebSocket.getAuthenticationResult());
    }

    private void enable() {
        obsWebSocket.getRecordingRequestSender().startRecording(this::response);
    }

    private void response(StartRecording.Response response) {
        obsWebSocket.getRecordingRequestSender().pauseRecording(this::response);
    }

    private void response(PauseRecording.Response response) {
        obsWebSocket.getRecordingRequestSender().resumeRecording(this::response);
    }

    private void response(ResumeRecording.Response response) {
        obsWebSocket.getRecordingRequestSender().stopRecording(this::response);
    }

    private void response(StopRecording.Response response) {
        System.out.println("Completed");
    }
}