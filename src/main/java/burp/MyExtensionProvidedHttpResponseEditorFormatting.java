package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import de.usd.cstchef.view.View;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

public class MyExtensionProvidedHttpResponseEditorFormatting implements ExtensionProvidedHttpResponseEditor
{
    public static ArrayList<MyExtensionProvidedHttpResponseEditorFormatting> cstcFormattingTabsinRepeater = new ArrayList<>();
    private final RawEditor responseEditor;
    private HttpRequestResponse requestResponse;
    private final MontoyaApi api;
    private final View view;

    MyExtensionProvidedHttpResponseEditorFormatting(EditorCreationContext creationContext, View view)
    {
        this.api = BurpUtils.getInstance().getApi();
        this.view = view;
        this.responseEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
        this.responseEditor.uiComponent().addFocusListener(new CstcFocusResponseListener(this));
    }

    public HttpRequestResponse getRequestResponse() {
        return requestResponse;
    }

    @Override
    public HttpResponse getResponse()
    {
        return requestResponse.response();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse)
    {
        this.requestResponse = requestResponse;
        ByteArray result = view.getFormatRecipePanel().bake(requestResponse.response().toByteArray(), requestResponse.request().toByteArray());
        this.responseEditor.setContents(result);
    }

    public void reapplyRecipe(){
        this.setRequestResponse(this.requestResponse);
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse)
    {
        return requestResponse.response() != null;
    }

    @Override
    public String caption()
    {
        return "CSTC Formatting";
    }

    @Override
    public Component uiComponent()
    {
        return responseEditor.uiComponent();
    }

    @Override
    public Selection selectedData()
    {
        return responseEditor.selection().isPresent() ? responseEditor.selection().get() : null;
    }

    @Override
    public boolean isModified()
    {
        return responseEditor.isModified();
    }

    private class CstcFocusResponseListener implements FocusListener{

        MyExtensionProvidedHttpResponseEditorFormatting responseEditorFormatting;

        public CstcFocusResponseListener(MyExtensionProvidedHttpResponseEditorFormatting responseEditorFormatting){
            this.responseEditorFormatting = responseEditorFormatting;
        }

        @Override
        public void focusGained(FocusEvent e) {
            this.responseEditorFormatting.reapplyRecipe();
            Logger.getInstance().log(("Reapplying in Response Editor"));
        }

        @Override
        public void focusLost(FocusEvent e) {
            return;
        }

    }
}

