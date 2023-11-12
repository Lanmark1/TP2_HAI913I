package util.graph;

public class WeightedEdge extends Edge {

    private double weight;

    public WeightedEdge(Vertex v1, Vertex v2, double weight) {
        super(v1, v2);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return getV1() + " -> " + getV2() + " : " + String.format("%1.3f", weight);
    }

    @Override
    public double getWeight() {
        return weight;
    }
}
