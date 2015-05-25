package org.jsonbuddy;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class JsonNode {
    public String toJson() {
        StringWriter res = new StringWriter();
        toJson(new PrintWriter(res));
        return res.toString();
    }

    public abstract void toJson(PrintWriter printWriter);

}