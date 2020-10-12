package weka.dl4j.inference;

/**
 * Simple immutable class to hold the necessary values for prediction
 * @author - Rhys Compton
 */
public class Prediction {

    /**
     * Stores the ID and Class name of the predicted class
     */
    protected PredictionClass predictionClass;

    /**
     * Probability of the predicted class
     */
    protected double classProbability;

    public Prediction(PredictionClass predictionClass, double classProbability) {
        this.predictionClass = predictionClass;
        this.classProbability = classProbability;
    }

    public Prediction(int classID, String className, double classProbability) {
        this.predictionClass = new PredictionClass(classID,className);
        this.classProbability = classProbability;
    }

    public PredictionClass getPredictionClass() {
        return predictionClass;
    }

    public int getClassID() {
        return this.predictionClass.getID();
    }

    public String getClassName() {
        return this.predictionClass.getName();
    }

    public double getClassProbability() {
        return classProbability;
    }

    public String toTableRowString(String lineFormat) {
        String classID = "" + getClassID();
        String probability = String.format("%.3f", getClassProbability() * 100);
        return String.format(lineFormat, classID, getClassName(), probability);
    }

    @Override
    public String toString() {
        return String.format("%s, Class prob = %.3f",
                this.predictionClass.toString(),
                getClassProbability());
    }
}
