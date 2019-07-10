package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionStringResolver {
    private static String connectionString;
    private static String endPoint, host, sharedAccessKeyName, sharedAccessKey;
    ConnectionStringResolver(String connectionString){
        this.connectionString = connectionString;
        endPoint = host = sharedAccessKeyName = sharedAccessKey = "";
        resolve();
    }

    private void resolve() {
        this.connectionString += ";";
        String pattern = "(.*)(Endpoint=.*;)(.*)(SharedAccessKeyName=.*;)(.*)(SharedAccessKey=.*;)(.*)";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(connectionString);

        if(m.find()) {

            endPoint = m.group(2);
            sharedAccessKeyName = m.group(4);
            sharedAccessKey = m.group(6);

            int start = endPoint.indexOf("//");
            int end = endPoint.lastIndexOf(";");
            if(start != -1 && end != -1) {
                if(endPoint.charAt(endPoint.length() - 2) == '/') end--;
                host = endPoint.substring(start + 2, end);
            }

            start = sharedAccessKeyName.indexOf("=");
            end = sharedAccessKeyName.lastIndexOf(";");
            if(start != -1 && end != -1) {
                sharedAccessKeyName = sharedAccessKeyName.substring(start + 1, end);
            }

            start = sharedAccessKey.indexOf("=");
            end = sharedAccessKey.lastIndexOf(";");
            if(start != -1 && end != -1) {
                sharedAccessKey = sharedAccessKey.substring(start + 1, end);
            }

        } else {
            System.out.println("Connectionstring resolve failed.");
        }
    }

    public String getHost() {
        return host;
    }

    public String getSasKeyName() {
        return sharedAccessKeyName;
    }

    public String getSasKey() {
        return sharedAccessKey;
    }
}