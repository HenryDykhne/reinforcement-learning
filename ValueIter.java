import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ValueIter {
    private static HashMap<State, Double> utility;
    private static String[] actions = {"e", "n", "w", "s"};
    private static String[] resultsOfActions = {"e", "n", "w", "s", "h"};
    private static ArrayList<State> states;
    private static int width;
    private static int depth;
    private static double epsilon;
    private static double gamma;
    public static void main(String[] args) {
        states = new ArrayList<>();
        parseMdp(args[0]);
        parseCtr(args[1]);
        valueIteration();
        System.out.println("width "+width);
        System.out.println("depth "+depth);
        System.out.println("gamma "+gamma);
        System.out.println("epsilon "+epsilon);
        for(int i = depth; i > 0; i--){
            for(int j = 1; j <= width; j++){
                System.out.print(String.format("%.2f", utility.get(getStateByCoords(j,i))) + "\t");
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

            System.out.println("depth "+depth);
        } while (del >= epsilon);
        return newUtilities;
    }

    private static Double max(State state) {
        double max = 0;
        for(String action: actions) {
            double temp = 0;
            for (String resultDirection: resultsOfActions) {
                State destination = stateFromDirection(state, resultDirection);
                if(destination != null && destination.isReachable()){
                    temp += state.getProbs().get(action).get(resultDirection) * utility.get(destination);
                }
            }
            if(temp > max) {
                max = temp;
            }
        }
        //System.out.println("max: " + max);
        return max;
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

