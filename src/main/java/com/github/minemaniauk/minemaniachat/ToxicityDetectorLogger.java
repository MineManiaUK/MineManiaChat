/*
 * MineManiaChat
 * Used for interacting with the database and message broker.
 *
 * Copyright (C) 2023  MineManiaUK Staff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.minemaniauk.minemaniachat;

import com.computerwhz.ToxicityDetector;
import com.computerwhz.ToxicityScore;

import java.text.DecimalFormat;
import java.util.Map;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ToxicityDetectorLogger {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final DecimalFormat DF = new DecimalFormat("0.000");

    private final String webhookUrl;

    private final ToxicityDetector detector;

    public ToxicityDetectorLogger(String webhookUrl, ToxicityDetector detector) {
        this.webhookUrl = webhookUrl;
        this.detector = detector;
    }

    public void log(String message, String playerName) {
        try {
            // Analyze
            ToxicityScore result = detector.analyze(message);

            // Format all scores as a simple bullet list (UPPERCASE)
            String scoresRaw = formatScores(result);                // lines with \n
            String scoreText = escapeJson(truncate(scoresRaw, 1000)); // Discord field.limit ≈ 1024

            // Escape + truncate message + name for JSON
            String safeMsg  = escapeJson(truncate(message, 1800));  // keep embed small
            String safeName = escapeJson(truncate(playerName, 128));

            // Color by toxicity (Discord embed color is decimal RGB)
            int color = pickColor(result);

            String payload =
                    "{"
                            + "\"embeds\":[{"
                            +   "\"title\":\"Toxicity Analysis\","
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

            try (Response res = HTTP.newCall(req).execute()) {
                if (!res.isSuccessful()) {
                    MineManiaChat.getInstance().getLogger()
                            .warn("Discord webhook failed: HTTP {} {}",
                                    res.code(),
                                    res.body() != null ? res.body().string() : "");
                }
            }
        } catch (IOException e) {
            MineManiaChat.getInstance().getLogger()
                    .warn("Could not send Discord log: {}", e.getMessage());
        } catch (Exception e) {
            MineManiaChat.getInstance().getLogger()
                    .error("Could not get toxicity score", e);
        }
    }

    private static String formatScores(ToxicityScore result) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : result.getScores().entrySet()) {
            double v = (e.getValue() == null) ? Double.NaN : e.getValue();
            sb.append("• ")
                    .append(e.getKey().toUpperCase())
                    .append(": ")
                    .append(DF.format(v))
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static int pickColor(ToxicityScore r) {
        double tox = r.toxicity().orElse(0.0);
        if (tox >= 0.80) return 0xE53935; // red
        if (tox >= 0.50) return 0xFB8C00; // orange
        return 0x43A047;                  // green
    }
}

