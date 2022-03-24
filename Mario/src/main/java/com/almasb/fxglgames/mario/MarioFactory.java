package com.almasb.fxglgames.mario;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.components.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.ui.FontType;
import com.almasb.fxglgames.mario.components.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxglgames.mario.MarioType.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MarioFactory implements EntityFactory {

    @Spawns("background")
    public Entity newBackground(SpawnData data) {
        int index = data.get("index");
        int speed = 11 - index;

        return entityBuilder()
                //.view(new ScrollingBackgroundView(texture("background/bg_" + index + ".png", getAppWidth(), getAppHeight()), 0.05 * speed))
                .zIndex(-1)
                .with(new IrremovableComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        return entityBuilder()
                .type(PLATFORM)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .build();
    }

    @Spawns("exitTrigger")
    public Entity newExitTrigger(SpawnData data) {
        return entityBuilder()
                .type(EXIT_TRIGGER)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("doorTop")
    public Entity newDoorTop(SpawnData data) {
        return entityBuilder()
                .type(DOOR_TOP)
                .from(data)
                .opacity(0)
                .build();
    }

    @Spawns("doorBot")
    public Entity newDoorBot(SpawnData data) {
        return entityBuilder()
                .type(DOOR_BOT)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .opacity(0)
                .with(new CollidableComponent(false))
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(16, 38), BoundingShape.box(6, 8)));

        // this avoids player sticking to walls
        physics.setFixtureDef(new FixtureDef().friction(0.0f));

        return entityBuilder()
                .type(PLAYER)
                .from(data)
                .bbox(new HitBox(new Point2D(5,5), BoundingShape.circle(12)))
                .bbox(new HitBox(new Point2D(10,25), BoundingShape.box(10, 17)))
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(new HPComponent(100))
                .with(new PlayerComponent())
                .build();
    }

    @Spawns("exitSign")
    public Entity newExit(SpawnData data) {
        return entityBuilder()
                .type(EXIT_SIGN)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("keyPrompt")
    public Entity newPrompt(SpawnData data) {
        return entityBuilder()
                .type(KEY_PROMPT)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("keyCode")
    public Entity newKeyCode(SpawnData data) {
        String key = data.get("key");

        KeyCode keyCode = KeyCode.getKeyCode(key);

        var lift = new LiftComponent();
        lift.setGoingUp(true);
        lift.yAxisDistanceDuration(6, Duration.seconds(0.76));

        return entityBuilder()
                .from(data)
                .view(new KeyView(keyCode, Color.YELLOW, 24))
                .with(lift)
                .zIndex(100)
                .build();
    }

    @Spawns("button")
    public Entity newButton(SpawnData data) {
        var keyEntity = getGameWorld().create("keyCode", new SpawnData(data.getX(), data.getY() - 50).put("key", "E"));
        keyEntity.getViewComponent().setOpacity(0);

        return entityBuilder()
                .type(BUTTON)
                .from(data)
                .viewWithBBox(texture("button.png", 20, 18))
                .with(new CollidableComponent(true))
                .with("keyEntity", keyEntity)
                .build();
    }

    @Spawns("messagePrompt")
    public Entity newMessagePrompt(SpawnData data) {
        var text = getUIFactoryService().newText(data.get("message"), Color.BLACK, FontType.GAME, 20.0);
        text.setStrokeWidth(2);

        return entityBuilder()
                .type(MESSAGE_PROMPT)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .view(text)
                .with(new CollidableComponent(true))
                .opacity(0)
                .build();
    }

    @Spawns("portal")
    public Entity newPortal(SpawnData data) {
        return entityBuilder()
                .type(PORTAL)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PortalComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("lift")
    public Entity newLift(SpawnData data) {
        var physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        boolean isGoingUp = data.hasKey("up") ? data.get("up") : true;

        var distance = (isGoingUp) ? data.getY() - data.<Integer>get("endY") : data.<Integer>get("endY") - data.getY();
        var speed = 100;
        var duration = Duration.seconds(distance / speed);

        return entityBuilder()
                .type(LIFT)
                .from(data)
                .bbox(new HitBox(new Point2D(0, 50), BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height") - 50)))
                .with(physics)
                .with(new PhysicsLiftComponent(duration, distance, isGoingUp))
                .build();
    }

    @Spawns("destrBox")
    public Entity newDestructibleBox(SpawnData data) {
        var comp = new DestructibleBoxComponent();

        var box = entityBuilder()
                .type(PLATFORM)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .with(comp)
                .build();

        box.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> comp.explode());

        return box;
    }

    @Spawns("coin")
    public Entity newCoin(SpawnData data) {
        return entityBuilder()
                .type(COIN)
                .from(data)
                .view(texture("coin.png").toAnimatedTexture(6, Duration.seconds(0.8)).loop())
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("question")
    public Entity newQuestion(SpawnData data) {
        return entityBuilder()
                .type(QUESTION)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("timeoutBox")
    public Entity newTimeoutBox(SpawnData data) {
        int duration = data.get("duration");

        Text text = getUIFactory().newText("", Color.WHITE, 32);
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1.5);

        var e = entityBuilder()
                .type(TIMEOUT_BOX)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new TimeoutBoxComponent(text, duration))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .build();

        e.setOnActive(() -> {
            e.getViewComponent().addChild(text);
        });

        return e;
    }

    @Spawns("lootBox")
    public Entity newLootBox(SpawnData data) {
        var e = entityBuilder()
                .type(LOOT_BOX)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new LootBoxComponent())
                .build();

        e.setOnActive(() -> {

        });

        return e;
    }

    @Spawns("jumpPad")
    public Entity newJumpPad(SpawnData data) {
        return entityBuilder()
                .type(JUMP_PAD)
                .from(data)
                .bbox(new HitBox(new Point2D(0, 50), BoundingShape.box(data.<Integer>get("width"), 20)))
                .with(new CollidableComponent(true))
                .with(new JumpPadComponent())
                .build();
    }

    @Spawns("catapult")
    public Entity newCatapult(SpawnData data) {
        return entityBuilder()
                .type(CATAPULT)
                .from(data)
                .view(texture("catapult.png", 140, 140))
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new CatapultComponent())
                .build();
    }

    @Spawns("catapultLineIndicator")
    public Entity newCatapultLineIndicator(SpawnData data) {
        var rect = new Rectangle(150, 10, null);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(2);

        var arrow1 = new Rectangle(25, 2.5, Color.BLUE);
        arrow1.setTranslateX(126);
        arrow1.setTranslateY(-5);
        arrow1.setRotate(20);

        var arrow2 = new Rectangle(25, 2.5, Color.BLUE);
        arrow2.setRotate(-20);
        arrow2.setTranslateX(126);
        arrow2.setTranslateY(12);

        return entityBuilder()
                .from(data)
                .view(new Group(rect, arrow1, arrow2))
                .with(new IrremovableComponent())
                .with(new CatapultLineIndicatorComponent())
                .zIndex(5000)
                .build();
    }

    @Spawns("playerGuard")
    public Entity newPlayerGuard(SpawnData data) {
        var rect = new Rectangle(15, 15, Color.RED);
        rect.setStrokeWidth(1.5);
        rect.setStroke(Color.BLUEVIOLET);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        return entityBuilder()
                .type(PLAYER_GUARD)
                .from(data)
                .view(rect)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with("isActivated", false)
                .with(
                        new LiftComponent()
                                .xAxisDistanceDuration(FXGLMath.random(5, 25), Duration.seconds(FXGLMath.random(0.5, 1.5)))
                                .yAxisDistanceDuration(FXGLMath.random(5, 25), Duration.seconds(FXGLMath.random(0.5, 1.5)))
                )
                .with(new CollidableComponent(true))
                .zIndex(2000)
                .build();
    }

    /* ENEMIES */

    @Spawns("enemyBox")
    public Entity newEnemyBox(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .bbox(new HitBox(new Point2D(10, 10), BoundingShape.box(data.<Integer>get("width") - 20, data.<Integer>get("height") - 20)))
                .with(new EnemyBoxComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("enemyZombie")
    public Entity newEnemyZombie(SpawnData data) {
        int patrolEndX = data.get("patrolEndX");

        var e = entityBuilder()
                .type(ENEMY)
                .from(data)
                .bbox(new HitBox(new Point2D(10, 20), BoundingShape.box(232 / 4 - 20, 390 / 4 - 20)))
                .with(new LiftComponent().xAxisDistanceDuration(patrolEndX - data.getX(), Duration.seconds(FXGLMath.random(1, 3))))
                .with(new EnemyZombieComponent())
                .with(new CollidableComponent(true))
                .build();

        // fix zombie's height
        e.setOnActive(() -> e.translateY(-25));

        return e;
    }

    @Spawns("enemyAttackZombie")
    public Entity newEnemyAttackZombie(SpawnData data) {
        var e = entityBuilder()
                .type(ENEMY)
                .from(data)
                .bbox(new HitBox(new Point2D(10, 20), BoundingShape.box(245 / 4 - 20, 352 / 4 - 20)))
                .with(new EnemyAttackZombieComponent())
                .with(new CollidableComponent(true))
                .build();

        // fix zombie's height
        e.setOnActive(() -> {
            e.translateY(-15);
        });

        return e;
    }

    @Spawns("enemyAttackZombieProjectile")
    public Entity newEnemyAttackZombieProjectile(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .viewWithBBox(texture("enemies/zombie/bone.gif").toAnimatedTexture(4, Duration.seconds(0.46)).loop())
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 350))
                .with(new OffscreenCleanComponent())
                .build();
    }

    @Spawns("enemyTurret")
    public Entity newEnemyTurret(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .view("enemies/turret/sprite_0.png")
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new EnemyTurretComponent())
                .with(new OffscreenPauseComponent())
                .build();
    }

    @Spawns("enemyTurretProjectile")
    public Entity newEnemyTurretProjectile(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .with("isProjectile", true)
                .viewWithBBox("enemies/turret/projectile.png")
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 750))
                .with(new OffscreenCleanComponent())
                .build();
    }

    @Spawns("enemyMissileTurret")
    public Entity newEnemyMissileTurret(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .view("enemies/turret/sprite_0.png")
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new EnemyMissileTurretComponent())
                .with(new OffscreenPauseComponent())
                .build();
    }

    // TODO: subtypes, e.g. enemy projectile?

    @Spawns("enemyMissileTurretProjectile")
    public Entity newEnemyMissileTurretProjectile(SpawnData data) {
        ParticleEmitter emitter = ParticleEmitters.newSmokeEmitter();
        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setSize(1, 4);
        emitter.setSpawnPointFunction((i) -> new Point2D(7, 13));
        emitter.setStartColor(Color.LIGHTGRAY);
        emitter.setEndColor(Color.BLACK);

        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .viewWithBBox(texture("enemies/turret/missile_turret_projectile.png").toAnimatedTexture(5, Duration.seconds(0.8)).loop())
                .with("isProjectile", true)
                .scale(1.5, 1.5)
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(new Point2D(0, -1), 400))
                .with(new EffectComponent())
                .with(new ParticleComponent(emitter))
                .with(new HomingMissileProjectileComponent())
                .build();
    }
}
