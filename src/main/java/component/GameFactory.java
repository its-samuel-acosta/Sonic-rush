package component;

import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.entity.EntityFactory;
import static com.almasb.fxgl.dsl.FXGL.*;
import java.util.UUID;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import component.Personajes.KnucklesComponent;
import component.Personajes.PlayerComponent;
import component.Personajes.SonicComponent;
import component.Personajes.TailsComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.component.Component;
import GameSettings.Player;
import com.almasb.fxgl.texture.Texture; // Importar Texture

public class GameFactory implements EntityFactory {
    
    public enum EntityType {
        PLAYER, FONDO, TIERRA, ROBOT_ENEMIGO, RING, AGUA, BASURA, ARBOL, PAPEL, CAUCHO, EGGMAN
    }

    /**
     * Método base privado para crear cualquier tipo de jugador.
     * Centraliza la configuración de física y componentes comunes.
     */
    private Player createPlayerBase(SpawnData data, PlayerComponent playerComponent) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(16, 38), BoundingShape.box(6, 8)));
        physics.setFixtureDef(new FixtureDef().friction(1.0f).restitution(0.0f));

        Player player = new Player();
        player.setType(EntityType.PLAYER);
        player.getBoundingBoxComponent().addHitBox(new HitBox(new Point2D(5, 5), BoundingShape.circle(12)));
        player.getBoundingBoxComponent().addHitBox(new HitBox(new Point2D(10, 25), BoundingShape.box(10, 17)));
        
        player.addComponent(physics);
        player.addComponent(new CollidableComponent(true));
        player.addComponent(new IrremovableComponent());
        player.addComponent(playerComponent); // Añade el componente específico del personaje

        player.setPosition(data.getX(), data.getY());
        player.getProperties().setValue("altura", 32.0);

        return player;
    }

    @Spawns("sonic")
    public Player sonic(SpawnData data) {
        return createPlayerBase(data, new SonicComponent());
    }

    @Spawns("tails")
    public Player tails(SpawnData data) {
        return createPlayerBase(data, new TailsComponent());
    }

    @Spawns("knuckles")
    public Player knuckles(SpawnData data) {
        return createPlayerBase(data, new KnucklesComponent());
    }

    // Nuevo método para la entidad "fondo"
    @Spawns("fondo")
    public Entity newBackground(SpawnData data) {
        // Carga la textura del fondo. Asegúrate de que "fondo.png" esté en assets/textures/
        Texture backgroundTexture = getAssetLoader().loadTexture("fondo.png"); 
        
        return entityBuilder(data)
                .view(backgroundTexture)
                .zIndex(-100) // Asegura que el fondo esté detrás de todo lo demás
                .build();
    }

    @Spawns("plataforma")
    public Entity newPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return entityBuilder(data)
                .type(EntityType.TIERRA)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physics)
                .build();
    }

    @Spawns("agua")
    public Entity newAgua(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return entityBuilder(data)
                .type(EntityType.AGUA)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physics)
                .build();
    }

     private Entity createItem(SpawnData data, EntityType type, Component itemComponent) {
        Entity item = entityBuilder(data)
                .type(type)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.circle(12)))
                .with(itemComponent)
                .with(new CollidableComponent(true))
                .build();
        // Manera unificada y recomendada de asignar propiedades
        item.getProperties().setValue("id", UUID.randomUUID().toString());
        return item;
    }

     @Spawns("ring")
    public Entity ring(SpawnData data) {
        return createItem(data, EntityType.RING, new component.Items.RingComponent());
    }

    @Spawns("basura")
    public Entity basura(SpawnData data) {
        Entity trash = createItem(data, EntityType.BASURA, new component.Items.TrashComponent());
        trash.getProperties().setValue("tipo", "basura");
        return trash;
    }

    @Spawns("papel")
    public Entity papel(SpawnData data) {
        Entity papel = entityBuilder(data)
                .type(EntityType.PAPEL)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.circle(12)))
                .with(new component.Items.PapelComponent())
                .with(new CollidableComponent(true))
                .with("trashId", UUID.randomUUID().toString())
                .build();
        papel.getProperties().setValue("id", java.util.UUID.randomUUID().toString());
        return papel;
    }

    @Spawns("caucho")
    public Entity caucho(SpawnData data) {
        Entity caucho = entityBuilder(data)
                .type(EntityType.CAUCHO)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.circle(12)))
                .with(new component.Items.CauchoComponent())
                .with(new CollidableComponent(true))
                .with("trashId", UUID.randomUUID().toString())
                .build();
        caucho.getProperties().setValue("id", java.util.UUID.randomUUID().toString());
        return caucho;
    }

    @Spawns("arbol")
    public Entity newArbol(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.ARBOL)
                .view("Escenario/arbol.png") 
                .zIndex(1)
                .build();
    }

    @Spawns("robotEnemigo")
    public Entity robotEnemigo(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Entity robot = entityBuilder(data)
                .type(EntityType.ROBOT_ENEMIGO)
                .bbox(new HitBox(new Point2D(10, 10), BoundingShape.circle(35))) // radio 32 centrada
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new component.Enemigos.RobotComponent())
                .with("altura", 100.0)
                .build(); 

        robot.getProperties().setValue("id", java.util.UUID.randomUUID().toString()); 

        return robot;
    }

    @Spawns("eggman")
    public Entity eggman(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        FixtureDef fixture = new FixtureDef();
        fixture.setRestitution(0.0f);
        physics.setFixtureDef(fixture);

        Entity eggman = entityBuilder(data)
                .type(EntityType.EGGMAN)
                .bbox(new HitBox(new Point2D(10, 10), BoundingShape.circle(40)))
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new component.Enemigos.EggmanComponent())
                .with("altura", 100.0)
                .build();

        eggman.getProperties().setValue("id", java.util.UUID.randomUUID().toString());

        return eggman;
    }
}
