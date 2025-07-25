package component.Enemigos;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;


//Clase EnemyComponent que define el comportamiento de los enemigos
@Required(PhysicsComponent.class)
public abstract class EnemyComponent extends Component {

    protected PhysicsComponent physics;
    protected double speed;
    protected double patrolDistance;
    protected int health;

    private double startX;
    private int direction = 1;
    private int cooldownFrames = 0;
    private static final int COOLDOWN_MAX = 15; // 15 frames de espera tras girar

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
        if (cooldownFrames > 0) {
            cooldownFrames--;
        }
        // Patrullaje
        physics.setVelocityX(speed * direction);

        //Evita que se quede trabado girando al colisionar
        if (Math.abs(entity.getX() - startX) >= patrolDistance && cooldownFrames == 0) {
            direction *= -1; // Invierte la dirección
            entity.setScaleX(entity.getScaleX() * -1);
            cooldownFrames = COOLDOWN_MAX;
        }

        // Evitar que los enemigos se atraviesen entre sí (solo para robots)
        if (entity.getType() == component.GameFactory.EntityType.ROBOT_ENEMIGO && cooldownFrames == 0) {
            entity.getWorld().getEntitiesByType(component.GameFactory.EntityType.ROBOT_ENEMIGO).stream()
                .filter(e -> e != entity && e.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent()))
                .findAny()
                .ifPresent(e -> {
                    direction *= -1;
                    entity.setScaleX(entity.getScaleX() * -1);
                    cooldownFrames = COOLDOWN_MAX;
                });
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