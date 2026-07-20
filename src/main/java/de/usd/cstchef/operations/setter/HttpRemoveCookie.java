package de.usd.cstchef.operations.setter;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Remove HTTP Cookie", category = OperationCategory.SETTER, description = "Remove the specified HTTP cookie.")
public class HttpRemoveCookie extends Operation {

    protected VariableTextField cookie;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        MessageType messageType = parseMessageType(input);

        String cookieName = cookie.getText();
        if(cookieName.isEmpty()) {
            return input;
        }

        if(messageType == MessageType.REQUEST) {
            HttpRequest request = factory.createHttpRequest(input);

            // has Cookie header
            if(request.hasHeader("Cookie")) {
                String cookies = request.headerValue("Cookie");
                // has this particular cookie set
                if(cookies.contains(cookieName + "=")) {
                    String[] c = cookies.split("; ");
                    cookies = "";
                    for(String cookie : c) {
                        if(cookie.matches("^" + cookieName + "=\\S+")) {
                            continue;
                        }
                        cookies = cookies.concat(cookie + "; ");
                    }
                    cookies = cookies.replaceAll(";\s$", "");
                    if(cookies.length() == 0) {
                        return request.withRemovedHeader("Cookie").toByteArray();
                    }
                    return request.withUpdatedHeader("Cookie", cookies).toByteArray();
                }
                // has this particular cookie not set
                else {
                    throw new IllegalArgumentException("Cookie not found.");
                }
            }
            // has no Cookie header
            else {
                throw new IllegalArgumentException("Cookie not found.");
            }
        }
        else if(messageType == MessageType.RESPONSE) {
            HttpResponse response = factory.createHttpResponse(input);
            // has cookie
            if(response.hasCookie(cookieName)) {
                String responseString = response.toString();
                return factory.createByteArray(responseString.replaceAll("Set-Cookie: " + cookieName + "=(.)*(\r\n|\n)", ""));
            }
            // no cookie
            else {
                throw new IllegalArgumentException("Cookie not found.");
            }
        }
        else {
            return parseRawMessage(input);
        }
    }


    @Override
    public void createUI() {
        this.cookie = new VariableTextField();

        this.addUIElement("Cookie Name", this.cookie);
    }
    
}
