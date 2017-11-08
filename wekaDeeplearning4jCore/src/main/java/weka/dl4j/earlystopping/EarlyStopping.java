package weka.dl4j.earlystopping;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.OptionMetadata;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Weka implementation of the EarlyStopping from Dl4j.
 *
 * @author Steven Lang
 */
@Slf4j
public class EarlyStopping implements OptionHandler, Serializable {

    /**
     * SerialVersionUID
     */
    private static final long serialVersionUID = 5248828973394650102L;

    /**
     * Maximum of epochs without improvement
     */
    private int maxEpochsNoImprovement = 5;

    /**
     * Counter for the number of epochs without improvement
     */
    private int countEpochsNoImprovement = 0;

    /**
     * Last best score
     */
    private double lastBestScore = Double.MAX_VALUE;

    /**
     * Percentage of the training data to use as validation set
     */
    private double validationSetPercentage = 10;

    /**
     * Validation dataset
     */
    private DataSetIterator valDataSetIterator;


    public EarlyStopping() {
        // Dummy constructor
    }

    /**
     * Constructor setting maxEpochsNoImprovement and validation split
     *
     * @param maxEpochsNoImprovement  Maximum numer of epochs with no improvement
     * @param validationSetPercentage Validation split percentage
     */
    public EarlyStopping(int maxEpochsNoImprovement, double validationSetPercentage) {
        this.maxEpochsNoImprovement = maxEpochsNoImprovement;
        this.validationSetPercentage = validationSetPercentage;
    }

    /**
     * Initialize the underlying dl4j EarlyStopping object
     *
     * @param dsIt DataSet trainIterator of the validation set
     */
    public void init(DataSetIterator dsIt) {
        this.valDataSetIterator = dsIt;
    }

    /**
     * Reset the counter
     */
    private void resetEpochCounter() {
        countEpochsNoImprovement = 0;
    }

    /**
     * Evaluate a model and check if the training should continue. Returns false
     * if the score has not improved for the given number of epochs. Else true
     *
     * @param model Model to be evaluated against the validation set
     * @return If training should continue or not
     */
    public boolean evaluate(ComputationGraph model) {
        try {
            // Do not evaluate if set to zero
            if (maxEpochsNoImprovement == 0) {
                return true;
            }

            // If validation dataset is empty, do not evaluate and just continue
            if (!valDataSetIterator.hasNext()) {
                return true;
            }

            double scoreSum = 0;
            int iterations = 0;

            // Iterate batches
            while (valDataSetIterator.hasNext()) {
                DataSet next = valDataSetIterator.next();
                scoreSum += model.score(next);
                iterations++;
            }

            // Get average score
            double score = 0;
            if (iterations != 0) {
                score = scoreSum / iterations;
            }
            if (score < lastBestScore) {
                resetEpochCounter();
                lastBestScore = score;
                return true;
            } else {
                countEpochsNoImprovement++;
                return countEpochsNoImprovement < maxEpochsNoImprovement;
            }

        } catch (Exception e) {
            log.error("Could not evaluate early stopping. Continuing training " +
                    "process", e);
            return true;
        } finally {
            valDataSetIterator.reset();
        }
    }

    @OptionMetadata(
            displayName = "max epochs with no improvement",
            description = "Terminate after N epochs in which the model has shown no improvement (default = 5).",
            commandLineParamName = "maxEpochsNoImprovement", commandLineParamSynopsis = "-maxEpochsNoImprovement <int>",
            displayOrder = 0)
    public void setMaxEpochsNoImprovement(int maxEpochsNoIMprovement) {
        if (maxEpochsNoIMprovement < 0) {
            throw new RuntimeException("Early stopping criterion must be at " +
                    "least zero or above. Negative values are not allowed.");
        }
        this.maxEpochsNoImprovement = maxEpochsNoIMprovement;
    }

    public int getMaxEpochsNoImprovement() {
        return maxEpochsNoImprovement;
    }


    @OptionMetadata(
            displayName = "validation set percentage (removed from training set)",
            description = "Percentage of training set to use for validation (default = 10).",
            commandLineParamName = "valPercentage", commandLineParamSynopsis = "-valPercentage <float>",
            displayOrder = 1)
    public void setValidationSetPercentage(double p) {
        if (Double.compare(p, 100) >= 0 || p < 0) {
            throw new RuntimeException("Validation split percentage must be in 0 < p < 100.");
        }
        this.validationSetPercentage = p;
    }

    public double getValidationSetPercentage() {
        return validationSetPercentage;
    }

//    public org.deeplearning4j.earlystopping.EarlyStopping getConf() {
//        return conf;
//    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     */
    @Override
    public Enumeration<Option> listOptions() {

        return Option.listOptionsForClass(this.getClass()).elements();
    }

    /**
     * Gets the current settings of the Classifier.
     *
     * @return an array of strings suitable for passing to setOptions
     */
    @Override
    public String[] getOptions() {

        return Option.getOptions(this, this.getClass());
    }

    /**
     * Parses a given list of options.
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    public void setOptions(String[] options) throws Exception {

        Option.setOptions(options, this, this.getClass());
    }
}
