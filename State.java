import java.util.HashMap;

public class State {
    private int xVal;
    private int yVal;
    private double reward;
    private boolean terminal;
    private boolean reachable;
    
    private HashMap<String, HashMap<String, Double>> actionProbabilitesBasedOnIntendedAction;

    public State(int x, int y, double reward, HashMap<String, HashMap<String, Double>> probs) {
        this.xVal = x;
        this.yVal = y;
        this.reward = reward;
        this.actionProbabilitesBasedOnIntendedAction = probs;
        this.reachable = true;
        this.terminal = false;

        if(reward == -99.0) {
            this.reachable = false;
        }
        
        if (reachable && actionProbabilitesBasedOnIntendedAction.get("w").get("w") == -99.0){
            this.terminal = true;
        }

    }

    public double getReward() {
        return reward;
    }

    public int getXCor() {
        return xVal;
    }

    public int getYCor() {
        return yVal;
    }


    public Boolean isTerminal() {
        return terminal;
    }

    public Boolean isReachable() {
        return reachable;
    }

    public HashMap<String, HashMap<String, Double>> getProbs() {
        return actionProbabilitesBasedOnIntendedAction;
    }
}
