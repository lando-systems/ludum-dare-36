package lando.systems.ld36.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld36.levels.Level;
import lando.systems.ld36.utils.Assets;
import lando.systems.ld36.utils.KeyMapping;

/**
 * Created by dsgraham on 8/27/16.
 */
public class Player extends GameObject {

    private static final float HIT_DELTA_X = 32;
    private static final float HIT_DELTA_Y = 32;

    public final float moveSpeed = 150;

    public boolean isMoving = false;
    public boolean isAttacking = false;
    public float timer = 0f;
    public MutableFloat animationTimer;
    public Animation walkAnimation;
    public Animation attackAnimation;
    public Rectangle footBounds;


    public int health = 100;
    public int deaths = 0;

    public Player(Level level){
        super(level);
        animationTimer = new MutableFloat(0f);
        walkAnimation = Assets.floppyWalk;
        tex = walkAnimation.getKeyFrame(timer);

        width = tex.getRegionWidth();
        height = tex.getRegionHeight();

        attackAnimation = Assets.floppyPunch;
        isFacingRight = true;
        footBounds = new Rectangle();
        hitBounds = new Rectangle(position.x + 15f, position.y + 4f, 30f, tex.getRegionHeight() - 8f);
    }

    public void update(float dt, float leftEdge){
        super.update(dt);
        if (dead){
            level.screen.screenShake.shake(1f);
            respawn();
        }

        timer += dt;
        isMoving = false;
        this.leftEdge = leftEdge;

        if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.JUMP)){
            jump();
        }

        if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.RIGHT) &&
                Assets.keyMapping.isActionPressed(KeyMapping.ACTION.LEFT)){
            // Do nothing
        } else if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.RIGHT)){
            position.x += moveSpeed * dt;
            isMoving = true;
            isFacingRight = true;
        } else if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.LEFT)){
            position.x -= moveSpeed * dt;
            isMoving = true;
            isFacingRight = false;
        }

        if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.UP)){
            position.y += moveSpeed * dt;
            isMoving = true;
        }
        if (Assets.keyMapping.isActionPressed(KeyMapping.ACTION.DOWN)){
            position.y -= moveSpeed * dt;
            isMoving = true;
        }


        if(Assets.keyMapping.isActionPressed(KeyMapping.ACTION.ATTACK) && !isAttacking) {
            isAttacking = true;
            animationTimer.setValue(0f);
            Tween.to(animationTimer, -1, attackAnimation.getAnimationDuration())
                .target(attackAnimation.getAnimationDuration())
                .setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        isAttacking = false;
                    }
                })
                .start(Assets.tween);

        }

        if (isAttacking) {
            tex = attackAnimation.getKeyFrame(animationTimer.floatValue());
        }
        else if (isMoving) {
            tex = walkAnimation.getKeyFrame(timer);
        }

        hitBounds.x = position.x + 15f;
        hitBounds.y = position.y + position.z;
    }

    public void render(SpriteBatch batch){
        super.render(batch);
    }

    public boolean doesHit(Enemy enemy) {
        if (enemy.position.y > (this.position.y - HIT_DELTA_Y)
         && enemy.position.y < (this.position.y + HIT_DELTA_Y)) {
            return (isFacingRight && (enemy.hitBounds.x > hitBounds.x) && (enemy.hitBounds.x < hitBounds.x + hitBounds.width + HIT_DELTA_X))
               || (!isFacingRight && (enemy.hitBounds.x + enemy.hitBounds.width < hitBounds.x + hitBounds.width)
                                  && (enemy.hitBounds.x + enemy.hitBounds.width > hitBounds.x - HIT_DELTA_X));
        }
        return false;
    }

    public void respawn(){
        dead = false;
        deaths++;
        position.z = 0;
        jumpCount =0;
        verticalVelocity = 0;
        position.x = lastSafePlace.get(0).x;
        position.y = lastSafePlace.get(0).y;
        invunerableTimer = INVULERABLITIYDELAY;
    }
}
