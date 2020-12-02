import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
public class ValueIter {
    private static final Double EQUALS_CONST = 0.0000001;
    private static HashMap<State, Double> utility;
    private static String[] actions = {"e", "n", "w", "s"};
    private static String[] resultsOfActions = {"e", "n", "w", "s", "h"};
    private static ArrayList<State> states;
    private static int width;
    private static int depth;
    private static double epsilon;
    private static double gamma;
    private static String optimalPolicy;
    public static void main(String[] args) {
        states = new ArrayList<>();
        parseMdp(args[0]);
        parseCtr(args[1]);
        
        System.out.println("Transition Model:");
        for(State state: states) {
            System.out.println("s = (" + state.getXCor() + "," + state.getYCor() + ")");
            for(String action: actions) {
                System.out.println("a = " + actionToDirection(action));
                System.out.println("\t" + state.getProbs().get(action).get("n"));
                System.out.println(state.getProbs().get(action).get("w") + "\t" + state.getProbs().get(action).get("h") + "\t" + state.getProbs().get(action).get("e"));
                System.out.println("\t" + state.getProbs().get(action).get("s"));
            }
            System.out.print("\n");
        }

        valueIteration();

        System.out.println("Optimal policy:");
        for(int i = depth; i > 0; i--){
            for(int j = 1; j <= width; j++){
                State tempState = getStateByCoords(j,i);
                if(tempState.isTerminal()){
                    System.out.print(String.format("%.2f", utility.get(getStateByCoords(j,i))) + "\t");
                } else if(!tempState.isReachable()) {
                    System.out.print("  \t");

                }else {
                    max(tempState);

                    System.out.print(optimalPolicy + "\t");

                }
                
            }
            System.out.print("\n");
        }
    }

    private static State getStateByCoords(int x, int y) {
        for(State state: states) {
            if(state.getXCor() == x && state.getYCor() == y){
                return state;
            }
        }
        return null;
    }

    private static void parseMdp(String mdpFilename) {
        try (Scanner scanner = new Scanner(new File(mdpFilename))) {
            String line = scanner.nextLine();

            //get width
            width = Integer.parseInt(line.split(" ")[0]);
            //get depth
            depth = Integer.parseInt(line.split(" ")[1]);
            
            for(int i = 0; i < width * depth; i++){
                //eat empty line
                scanner.nextLine();
                
                //get coordiantes
                line = scanner.nextLine();
                int xCor = Integer.parseInt(line.split(" ")[0]);
                int yCor = Integer.parseInt(line.split(" ")[1]);
                
                //get reward
                line = scanner.nextLine();
                double reward = Double.parseDouble(line.split(" ")[0]);

                
                HashMap<String, HashMap<String, Double>> probs = new HashMap<>();
                for(int j = 0; j < 4; j++) {
                    line = scanner.nextLine();
                    String intendedAction = line.split("=")[1];
                    probs.put(intendedAction, new HashMap<>());
                    for(int k = 0; k < 5; k++){
                        probs.get(intendedAction).put(resultsOfActions[k], Double.parseDouble(line.split("\\s+")[k]));
                    }
                }
                State newState = new State(xCor, yCor, reward, probs);
                states.add(newState);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void parseCtr(String ctrFilename) {
        try (Scanner scanner = new Scanner(new File(ctrFilename))) {
            String line;

            // get gamma 
            line = scanner.nextLine();
            gamma = Double.parseDouble(line.split(" ")[0]);

            // get epsilon 
            line = scanner.nextLine();
            epsilon = Double.parseDouble(line.split(" ")[0]);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<State, Double> valueIteration() {
        //init state utilities to 0
        HashMap<State, Double> newUtilities = new HashMap<>();
        states.forEach(state -> {
            if(!state.isTerminal()){
                newUtilities.put(state, 0.0);
            } else {
                newUtilities.put(state,state.getReward());
            }
        });
        double del;
        int round = 1;
        do {
            utility = (HashMap<State, Double>) newUtilities.clone();
            del = 0.0;
            for(State state : states) {
                if(!state.isTerminal() && state.isReachable()) {
                    newUtilities.put(state, state.getReward() + gamma * max(state));
                }
                if(Math.abs(newUtilities.get(state) - utility.get(state)) > del) {
                    del = Math.abs(newUtilities.get(state) - utility.get(state));
                }
            }

            //print state utilities
            System.out.println("Round " + round + " State Utility (delta = " + String.format("%.7f", del) + "):");
            for(int i = depth; i > 0; i--){
                for(int j = 1; j <= width; j++){
                    State tempState = getStateByCoords(j,i);
                    if(!tempState.isReachable()) {
                        System.out.print("  \t");
                    } else {
                        System.out.print(String.format("%.2f", utility.get(getStateByCoords(j,i))) + "\t");
                    } 
                }
                System.out.print("\n");
            }
            round++;

        } while (del >= epsilon);
        return newUtilities;
    }

    private static Double max(State state) {
        double max = Double.NEGATIVE_INFINITY;
        ArrayList<Double> valArray = new ArrayList<>();
        for(String action: actions) {
            double temp = 0;
            for (String resultDirection: resultsOfActions) {
                State destination = stateFromDirection(state, resultDirection);
                if(destination != null && destination.isReachable()){
                    temp += state.getProbs().get(action).get(resultDirection) * utility.get(destination);
                }
            }
            valArray.add(temp);

            //System.out.println("temp: " + temp);
            //System.out.println("max: " + max);
            if(temp > max) {
                optimalPolicy = actionToSymbol(action);
                max = temp;
            }

            

        }
        if(Math.abs(valArray.get(0) - valArray.get(1)) < EQUALS_CONST && Math.abs(valArray.get(1) - valArray.get(2)) < EQUALS_CONST && Math.abs(valArray.get(2) - valArray.get(3)) < EQUALS_CONST) {
            optimalPolicy = "+ ";
        }
        return max;
    }

    private static String actionToSymbol(String action) {
        String symbol = null;
        if(action.equals("e")) {
            symbol = "->";
        } else if(action.equals("n")){
            symbol = "^ ";
        } else if(action.equals("w")){
            symbol = "<-";
        } else if(action.equals("s")){
            symbol = "v ";
        }
        return symbol;
    }

    private static String actionToDirection(String action) {
        String symbol = null;
        if(action.equals("e")) {
            symbol = "right";
        } else if(action.equals("n")){
            symbol = "up";
        } else if(action.equals("w")){
            symbol = "left";
        } else if(action.equals("s")){
            symbol = "down";
        }
        return symbol;
    }


    private static State stateFromDirection(State startState, String direction) {
        if(direction.equals("e")) {
            return getStateByCoords(startState.getXCor() + 1, startState.getYCor());
        } else if(direction.equals("n")) {
            return getStateByCoords(startState.getXCor(), startState.getYCor() + 1);
        } else if(direction.equals("w")) {
            return getStateByCoords(startState.getXCor() - 1, startState.getYCor());
        } else if(direction.equals("s")) {
            return getStateByCoords(startState.getXCor(), startState.getYCor() - 1);
        } else {
            return startState;
        }
        
    }

}

