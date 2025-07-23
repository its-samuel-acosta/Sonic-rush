package GameSettings;

/**
 * Representa una entrada de puntuación con el nombre del jugador y su puntaje.
 * Implementa Comparable para permitir la ordenación por puntuación.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {
    private String playerName;
    private int score;

    /**
     * Constructor para crear una nueva entrada de puntuación.
     * @param playerName El nombre del jugador.
     * @param score La puntuación obtenida.
     */
    public ScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    /**
     * Obtiene el nombre del jugador.
     * @return El nombre del jugador.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Obtiene la puntuación.
     * @return La puntuación.
     */
    public int getScore() {
        return score;
    }

    /**
     * Compara esta entrada de puntuación con otra para fines de ordenación.
     * Las puntuaciones se ordenan en orden descendente (mayor puntuación primero).
     * Si las puntuaciones son iguales, se ordenan alfabéticamente por nombre.
     * @param other La otra entrada de puntuación con la que comparar.
     * @return Un valor negativo si esta puntuación es mayor, positivo si es menor, o cero si son iguales.
     */
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

    /**
     * Devuelve una representación en cadena de la entrada de puntuación.
     * @return Una cadena formateada con el nombre del jugador y su puntuación.
     */
    @Override
    public String toString() {
        return String.format("%s: %d", playerName, score);
    }
}