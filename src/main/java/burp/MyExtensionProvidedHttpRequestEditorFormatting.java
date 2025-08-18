package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import de.usd.cstchef.view.View;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class MyExtensionProvidedHttpRequestEditorFormatting implements ExtensionProvidedHttpRequestEditor
{
    private final RawEditor requestEditor;
    private HttpRequestResponse requestResponse;
    private final MontoyaApi api;
    private final View view;

    MyExtensionProvidedHttpRequestEditorFormatting(EditorCreationContext creationContext, View view)
    {
        this.api = BurpUtils.getInstance().getApi();
        this.view = view;        
        this.requestEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
        this.requestEditor.uiComponent().addFocusListener(new CstcFocusRequestListener(this));
    }

    @Override
    public HttpRequest getRequest()
    {
        return requestResponse.request();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse)
    {
        this.requestResponse = requestResponse;
        ByteArray result = view.getFormatRecipePanel().bake(requestResponse.request().toByteArray(), requestResponse.request().toByteArray());
        this.requestEditor.setContents(result);
    }

    public void reapplyRecipe(){
        this.setRequestResponse(this.requestResponse);
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse)
    {
        return requestResponse.request() != null;
    }

    @Override
    public String caption()
    {
        return "CSTC Formatting";
    }

    @Override
    public Component uiComponent()
    {
        return requestEditor.uiComponent();
    }

    @Override
    public Selection selectedData()
    {
        return requestEditor.selection().isPresent() ? requestEditor.selection().get() : null;
    }

    @Override
    public boolean isModified()
    {
        return requestEditor.isModified();
    }

    private class CstcFocusRequestListener implements FocusListener{

        MyExtensionProvidedHttpRequestEditorFormatting requestEditorFormatting;

        public CstcFocusRequestListener(MyExtensionProvidedHttpRequestEditorFormatting requestEditorFormatting){
            this.requestEditorFormatting = requestEditorFormatting;
        }

        @Override
        public void focusGained(FocusEvent e) {            
            this.requestEditorFormatting.reapplyRecipe();
            Logger.getInstance().log(("Reapplying in Request Editor"));
        }

        @Override
        public void focusLost(FocusEvent e) {
            //Not needed
            return;
        }

    }

}