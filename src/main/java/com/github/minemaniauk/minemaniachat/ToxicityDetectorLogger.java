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
import com.eduardomcb.discord.webhook.WebhookClient;
import com.eduardomcb.discord.webhook.WebhookManager;
import com.eduardomcb.discord.webhook.models.Embed;
import com.eduardomcb.discord.webhook.models.Field;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToxicityDetectorLogger {

    private final DecimalFormat DF = new DecimalFormat("0.000");
    private final String discordUrl;
    private final ToxicityDetector detector;

    public ToxicityDetectorLogger(String discordUrl) throws Exception {
        this.discordUrl = discordUrl;
        this.detector = new ToxicityDetector();
    }

    public void log(String message, String userName) {

        ToxicityScore result;
        try {
            result = detector.analyze(message);
        }
        catch (Exception e) {
            MineManiaChat.getInstance().getLogger().error("Could not get toxicity score");
            MineManiaChat.getInstance().getLogger().error(e.getMessage());
            MineManiaChat.getInstance().getLogger().error(e.getStackTrace().toString());
            return;
        }

        WebhookManager webhook = new WebhookManager()
                .setChannelUrl(this.discordUrl)
                .setListener(new WebhookClient.Callback() {
                    @Override
                    public void onSuccess(String r) { }

                    @Override
                    public void onFailure(int code, String e) {
                        MineManiaChat.getInstance().getLogger().error("Failed to send discord log {} {}", code, e);
                    }
                });

        List<Field> fields = List.of(new Field("Scores", formatScores(result), false));
        Embed embed = new Embed()
                .setTitle("Toxicity analysis")
                .setDescription("Player: " + userName + "\n" + message)
                .setFields(fields.toArray(new Field[0]));

        webhook.setEmbeds(new Embed[]{ embed });
        webhook.exec();
    }

    public String formatScores(ToxicityScore result) {
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

}
