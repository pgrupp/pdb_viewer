package pdbview3d;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import pdbmodel.Residue;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Stacked Bar chart to present some stats of the loaded PDB file.
 *
 * @author Patrick Grupp
 */
public class MyStackedBarChart extends Group {

    final private static String alpha = "Alpha helix";
    final private static String beta = "Beta sheet";
    final private static String coil = "Coil";
    final private CategoryAxis xAxis = new CategoryAxis();
    final private NumberAxis yAxis = new NumberAxis();
    private StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);


    public void initialize(HashMap<Residue.AminoAcid, Integer> aminoAcidCountAlpha,
                           HashMap<Residue.AminoAcid, Integer> aminoAcidCountBeta,
                           HashMap<Residue.AminoAcid, Integer> aminoAcidCountCoil,
                           ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty heightProperty) {
        stackedBarChart.setTitle("Amino Acids in Secondary Structures");
        xAxis.setLabel("Secondary Structure");
        xAxis.setCategories(FXCollections.observableArrayList(Arrays.asList(alpha, beta, coil)));
        yAxis.setLabel("#Amino Acids");
        for (Residue.AminoAcid aaType : Residue.AminoAcid.values()) {
            if (aminoAcidCountAlpha.containsKey(aaType) ||
                    aminoAcidCountBeta.containsKey(aaType) ||
                    aminoAcidCountCoil.containsKey(aaType)) {
                XYChart.Series<String, Number> current = new XYChart.Series<>();
                current.setName(Residue.getName(aaType));
                XYChart.Data<String, Number> data;
                if (aminoAcidCountAlpha.containsKey(aaType)) {
                    data = new XYChart.Data<>(alpha, aminoAcidCountAlpha.get(aaType));
                    current.getData().add(data);
                    Tooltip.install(data.getNode(), new Tooltip(Residue.getName(aaType) + ", #Occurences: " + aminoAcidCountAlpha.get(aaType)));
                }
                if (aminoAcidCountBeta.containsKey(aaType)) {
                    data = new XYChart.Data<>(beta, aminoAcidCountBeta.get(aaType));
                    current.getData().add(data);
                    Tooltip.install(data.getNode(), new Tooltip(Residue.getName(aaType) + ", #Occurences: " + aminoAcidCountBeta.get(aaType)));

                }
                if (aminoAcidCountCoil.containsKey(aaType)) {
                    data = new XYChart.Data<>(coil, aminoAcidCountCoil.get(aaType));
                    current.getData().add(data);
                    Tooltip.install(data.getNode(), new Tooltip(Residue.getName(aaType) + ", #Occurences: " + aminoAcidCountCoil.get(aaType)));
                }
                stackedBarChart.getData().add(current);
            }
        }
        stackedBarChart.minWidthProperty().bind(Bindings.subtract(widthProperty, 50));
        stackedBarChart.minHeightProperty().bind(Bindings.subtract(heightProperty, 50));
        this.getChildren().add(stackedBarChart);
    }

    /**
     * Removes the bar plot, so it can be initialized again.
     */
    public void reset() {
        stackedBarChart = new StackedBarChart<String, Number>(xAxis,yAxis);
        this.getChildren().clear();
    }
}
