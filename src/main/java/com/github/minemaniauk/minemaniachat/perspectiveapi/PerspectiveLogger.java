package com.github.minemaniauk.minemaniachat.perspectiveapi;

import com.computerwhz.Attribute;
import com.computerwhz.PerspectiveClient;
import com.computerwhz.PerspectiveScore;
import com.github.minemaniauk.minemaniachat.MineManiaChat;
import okhttp3.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

public class PerspectiveLogger {

    private static final DecimalFormat DF = new DecimalFormat("0.000");
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final PerspectiveClient pc;
    private final OkHttpClient http;
    private final String webhookUrl;

    public PerspectiveLogger(String apiKey, String discordWebhookUrl) {
        if (apiKey == null || apiKey.isBlank() || discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            MineManiaChat.getInstance().getLogger()
                    .warn("[MineManiaChat] PerspectiveLogger not initialized: missing API key or webhook URL.");
            this.pc = null;
            this.http = null;
            this.webhookUrl = null;
            return;
        }
        this.pc = new PerspectiveClient(apiKey);
        this.http = new OkHttpClient();
        this.webhookUrl = discordWebhookUrl;
    }

    public void log(String playerName, String message) {
        if (pc == null || http == null || webhookUrl == null) return;
        if (message == null || message.isBlank()) return;

        try {
            PerspectiveScore result = pc.analyze(message, Arrays.asList(Attribute.values()));

            StringBuilder scoreText = new StringBuilder();
            for (Map.Entry<String, Double> e : result.getScores().entrySet()) {
                double v = e.getValue() == null ? Double.NaN : e.getValue();
                scoreText.append("• **").append(e.getKey()).append("**: ").append(DF.format(v)).append("\\n");
            }

            int color = colorForToxicity(result.toxicity().orElse(Double.NaN));

            String safeMsg = escapeJson(truncate(message, 1800));
            String safeName = escapeJson(playerName);

            String payload =
                    "{"
                            + "\"embeds\":[{"
                            +   "\"title\":\"Perspective Analysis\","
                            +   "\"description\":\"**Player:** " + safeName + "\\n\\n" + safeMsg + "\","
                            +   "\"color\":" + color + ","
                            +   "\"fields\":[{"
                            +       "\"name\":\"Scores\","
                            +       "\"value\":\"" + scoreText + "\","
                            +       "\"inline\":false"
                            +   "}]"
                            + "}]"
                            + "}";

            Request req = new Request.Builder()
                    .url(webhookUrl)
                    .post(RequestBody.create(payload, JSON))
                    .build();

            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful()) {
                    MineManiaChat.getInstance().getLogger()
                            .warn("[MineManiaChat] Discord webhook failed: HTTP {} {}", res.code(),
                                    res.body() != null ? res.body().string() : "");
                }
            }

        } catch (IOException e) {
            MineManiaChat.getInstance().getLogger()
                    .warn("Could not log Perspective: {}", e.getMessage());
        }
    }

    // ---------- helpers ----------

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String escapeJson(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"':  out.append("\\\""); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
            }
        }
        return out.toString();
    }

    private static int colorForToxicity(double tox) {
        if (Double.isNaN(tox)) return 0x9E9E9E; // gray
        if (tox >= 0.85) return 0xE53935;       // red
        if (tox >= 0.70) return 0xFB8C00;       // orange
        if (tox >= 0.40) return 0xFDD835;       // yellow
        return 0x43A047;                        // green
    }
}
