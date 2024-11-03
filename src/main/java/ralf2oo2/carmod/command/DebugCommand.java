package ralf2oo2.carmod.command;

import com.matthewperiut.retrocommands.api.Command;
import com.matthewperiut.retrocommands.util.SharedCommandSource;
import ralf2oo2.carmod.client.render.DebugRenderer;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand implements Command {
    private String[] options = new String[]{"debug"};
    private String[] debugOptions = new String[]{"sphere", "mesh", "bounds", "shadowmesh"};
    @Override
    public void command(SharedCommandSource commandSource, String[] parameters) {
        System.out.println(commandSource.getName());
        if(parameters.length == 1) return;
        switch (parameters[1]){
            case "debug":
                if(parameters.length == 2) return;
                switch (parameters[2]){
                    case "sphere":
                        DebugRenderer.renderSpheres = !DebugRenderer.renderSpheres;
                        break;
                    case "mesh":
                        DebugRenderer.renderMesh = !DebugRenderer.renderMesh;
                        break;
                    case "bounds":
                        DebugRenderer.renderBounds = !DebugRenderer.renderBounds;
                        break;
                    case "shadowmesh":
                        DebugRenderer.renderShadowMesh = !DebugRenderer.renderShadowMesh;
                        break;
                }
                break;
        }
    }

    @Override
    public String name() {
        return "vehicle";
    }

    @Override
    public String[] suggestion(SharedCommandSource source, int parameterNum, String currentInput, String totalInput) {
        System.out.println(currentInput);
        System.out.println(totalInput);
        if(parameterNum == 1){
            List<String> suggestions = new ArrayList<>();
            for(int i = 0; i < options.length; i++){
                if(options[i].startsWith(currentInput)){
                    suggestions.add(options[i].substring(currentInput.length(), options[i].length()));
                }
            }
            return suggestions.toArray(new String[0]);
        }
        if(parameterNum == 2){
            List<String> suggestions = new ArrayList<>();
            for(int i = 0; i < debugOptions.length; i++){
                if(debugOptions[i].startsWith(currentInput)){
                    suggestions.add(debugOptions[i].substring(currentInput.length(), debugOptions[i].length()));
                }
            }
            return suggestions.toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public void manual(SharedCommandSource commandSource) {

    }
}
