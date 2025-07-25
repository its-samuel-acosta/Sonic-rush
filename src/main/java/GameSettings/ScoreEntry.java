package GameSettings;



 //RClase que representa una entrada de puntuación con el nombre del jugador y su puntaje
 //Implementa Comparable para permitir la ordenacion natural por puntuación
public class ScoreEntry implements Comparable<ScoreEntry> {
    private String playerName;
    private int score;

    //constructor
    public ScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    //Getters
    public String getPlayerName() {
        return playerName;
    }


    public int getScore() {
        return score;
    }


    //Compara la entrada de puntuacion con otra para ordenar
    //Ordena de mayor puntuacion a menor
    //si hay puntuaciones iguales ordena por nombre de jugador
    @Override
    public int compareTo(ScoreEntry other) {
        // Ordenar por puntuación de forma descendente
        int scoreComparison = Integer.compare(this.score, other.score);
        if (scoreComparison != 0) {
            return scoreComparison; // Invertir para orden descendente
        }
        // Si las puntuaciones son iguales, ordenar por nombre alfabéticamente
        return this.playerName.compareToIgnoreCase(other.playerName);
    }

    //Muestra el nombre del jugador y su puntuacion
    @Override
    public String toString() {
        return String.format("%s: %d", playerName, score);
    }
}