package component.Enemigos;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;

/**
 * Clase base abstracta para componentes de enemigos que patrullan.
 * Contiene la lógica compartida de movimiento y vida para evitar duplicación de código.
 */
@Required(PhysicsComponent.class)
public abstract class EnemyComponent extends Component {

    protected PhysicsComponent physics;
    protected double speed;
    protected double patrolDistance;
    protected int health;

    private double startX;
    private int direction = 1;

    public EnemyComponent(double speed, double patrolDistance, int health) {
        this.speed = speed;
        this.patrolDistance = patrolDistance;
        this.health = health;
    }

    @Override
    public void onAdded() {
        // Guarda la posición inicial para un patrullaje consistente.
        startX = entity.getX();
        physics = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public void onUpdate(double tpf) {
        // Patrullaje
        physics.setVelocityX(speed * direction);

        // Lógica de patrullaje mejorada para evitar el "drifting".
        if (Math.abs(entity.getX() - startX) >= patrolDistance) {
            direction *= -1; // Invierte la dirección
            // Se invierte la escala para que el sprite mire en la dirección correcta
            entity.setScaleX(entity.getScaleX() * -1);
        }
    }

    public int getVidas() {
        return health;
    }

    public void restarVida() {
        if (health > 0) {
            health--;
        }
    }

    public boolean estaMuerto() {
        return health <= 0;
    }
}