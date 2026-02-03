package com.callcenter.callcenterwas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ARS Simulator CLI (Korean Version)
 * Simulates a phone call interaction for card loss reporting.
 */
public class ArsSimulator {

    private static final String BASE_URL = System.getProperty("ars.api.base-url",
            "http://localhost:8082/api/v1/ars");
    private static final Scanner scanner = new Scanner(System.in);
    private static String customerRef = null;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Continue 카드 분실 신고 ARS 시스템");
        System.out.println("==========================================");
        System.out.println("");

        sleep(500);
        System.out.println("[ARS] 안녕하세요. Continue 카드 분실 신고 ARS 시스템입니다. 현재 고객 정보를 확인 중입니다.");
        sleep(1000);

        // 1. Identify Customer by ANI
        System.out.println("[시뮬레이션] 휴대폰 번호를 입력해 주세요 (예: 01012345678):");
        System.out.print("> ");
        String ani = scanner.nextLine();
        System.out.println("");

        String identifyResponse = post("/identify", "{\"phoneNumber\":\"" + ani + "\"}");
        System.out.println("[DEBUG] Server Response: " + identifyResponse);

        String name = null;
        if (identifyResponse.contains("\"found\":true")) {
            name = extract(identifyResponse, "name");
            customerRef = extract(identifyResponse, "customerRef");

            sleep(500);
            System.out.println("[ARS] " + name + " 고객님, 안녕하세요.");
        } else {
            System.out.println("[ARS] 등록된 고객 정보를 찾을 수 없습니다. 상담원 연결을 원하시면 0번을 눌러주세요.");
            return;
        }

        // 2. Identity Confirmation
        sleep(500);
        System.out.println("[ARS] 본인이 맞으시면 1번, 아니면 2번을 눌러주세요.");
        System.out.print("> ");
        String confirm = scanner.nextLine();
        System.out.println("");

        if (!"1".equals(confirm)) {
            System.out.println("[ARS] 본인 확인이 되지 않았습니다. 상담원에게 연결합니다.");
            return;
        }

        // 3. PIN Verification (3-Tier Security)
        sleep(500);
        System.out.println("[ARS] 카드 비밀번호 4자리를 입력해 주세요.");
        System.out.print("> ");
        String pin = scanner.nextLine();
        System.out.println("");

        // Simulate secure transmission
        // System.out.println("[System] (PIN 암호화 및 은행 서버 검증 중...)");
        String verifyResponse = post("/verify-pin",
                "{\"customerRef\":\"" + customerRef + "\", \"customerName\":\"" + name + "\", \"pin\":\"" + pin
                        + "\"}");

        String caseId = extract(verifyResponse, "caseId");

        if (verifyResponse.contains("\"status\":\"SUCCESS\"")) {
            sleep(500);
            System.out.println("[ARS] 본인 확인이 완료되었습니다.");
        } else {
            System.out.println("[ARS] 비밀번호가 일치하지 않습니다. 다시 시도해 주세요.");
            return;
        }

        // 4. Card Selection
        List<String> cards = extractList(verifyResponse, "cardNo");
        List<String> cardRefs = extractList(verifyResponse, "cardRef");
        List<String> statuses = extractList(verifyResponse, "status");

        sleep(500);
        System.out.println("[ARS] 현재 사용 중인 카드가 " + cards.size() + "장 있습니다.");
        System.out.println("------------------------------------------");

        for (int i = 0; i < cards.size(); i++) {
            String status = statuses.size() > i ? statuses.get(i) : "ACTIVE";
            String statusText = "LOST".equals(status) ? "(분실 신고됨)" : "(정상 사용 중)";
            System.out.println((i + 1) + "번. " + cards.get(i) + " " + statusText);
        }
        System.out.println("------------------------------------------");

        sleep(500);
        System.out.println("[ARS] 분실 신고할 카드 번호를 선택해 주세요.");
        System.out.println("[ARS] 종료를 원하시면 0번을 눌러주세요.");
        System.out.print("> ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            choice = -1;
        }
        System.out.println("");

        List<String> selectedRefs = new ArrayList<>();
        if (choice > 0 && choice <= cards.size()) {
            String selectedStatus = statuses.size() > choice - 1 ? statuses.get(choice - 1) : "ACTIVE";
            if ("LOST".equals(selectedStatus)) {
                System.out.println("[ARS] 이미 분실 신고가 접수된 카드입니다.");
                post("/close-case", "{\"caseId\":\"" + caseId + "\", \"note\":\"분실 신고 카드 재선택 (종료)\"}");
                System.out.println("[System] (통화 종료)");
                return;
            }
            selectedRefs.add(cardRefs.get(choice - 1));
        } else if (choice == 0) {
            System.out.println("[ARS] 이용해 주셔서 감사합니다.");
            post("/close-case", "{\"caseId\":\"" + caseId + "\", \"note\":\"사용자 종료\"}");
            return;
        } else {
            System.out.println("[ARS] 잘못된 입력입니다. 통화를 종료합니다.");
            post("/close-case", "{\"caseId\":\"" + caseId + "\", \"note\":\"잘못된 입력으로 인한 종료\"}");
            return;
        }

        // 5. Report Loss
        sleep(500);
        System.out.println("[ARS] 선택하신 카드에 대해 분실 신고를 진행합니다.");
        System.out.println("[ARS] 잠시만 기다려 주세요...");
        System.out.println("");

        sleep(1500); // Simulate processing time

        String refsJson = "[\"" + String.join("\",\"", selectedRefs) + "\"]";
        String reportResponse = post("/report-loss",
                "{\"customerRef\":\"" + customerRef + "\", \"caseId\":\"" + caseId + "\", \"selectedCardRefs\":"
                        + refsJson + "}");

        if (reportResponse.contains("\"success\":true")) {
            // Generate a random receipt number for realism
            System.out.println("[ARS] 분실 신고가 정상적으로 접수되었습니다.");
            System.out.println("[ARS] 이용해 주셔서 감사합니다.");
        } else {
            System.out.println("[ARS] 시스템 오류가 발생했습니다. 고객센터로 문의해 주세요.");
        }

        System.out.println("");
        System.out.print("계속하려면 아무 키나 누르십시오 . . .");
        scanner.nextLine();
    }

    private static String post(String path, String json) {
        try {
            URL url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("utf-8"));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            // For checking connection issues quietly
            return "{\"success\":false, \"status\":\"ERROR\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private static String extract(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static List<String> extractList(String json, String key) {
        List<String> list = new ArrayList<>();
        // Simple regex to extract list items (assuming simple string list or key in
        // distinct objects)
        // Since the backend returns cards as a list of objects or similar, we iterate
        Pattern pattern = Pattern.compile("\"" + key + "\":\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
