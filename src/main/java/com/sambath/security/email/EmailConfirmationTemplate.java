package com.sambath.security.email;

public class EmailConfirmationTemplate {
    public static String confirmEmailTemplate(String name, String link) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Email Confirmation</title>\n" +
                "    <style>\n" +
                "        /* Define button styles */\n" +
                "        .button {\n" +
                "            background-color: #3498db;\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            padding: 12px 24px;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "            display: inline-block;\n" +
                "            font-size: 16px;\n" +
                "            margin: 4px 2px;\n" +
                "            cursor: pointer;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Confirmation Email</h2>\n" +
                "    <p>Hello, " + name + ",</p>\n" +
                "    <p>Thank you for registering. Please click the button below to confirm your email:</p>\n" +
                "    <p>\n" +
                "        <a href=\"" + link + "\" class=\"button\">Confirm Email</a>\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        Note: This link will expire in 15 minutes.\n" +
                "    </p>\n" +
                "</body>\n" +
                "</html>";
    }
}
