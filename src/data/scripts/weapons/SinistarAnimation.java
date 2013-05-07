package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class SinistarAnimation implements EveryFrameWeaponEffectPlugin
{
    private static final float FRAMES_PER_SECOND = 4f;
    private static final float TIME_BETWEEN_FRAMES = 1.0f / FRAMES_PER_SECOND;
    private float timeSinceLastFrame;
    private int curFrame = 1;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused())
        {
            return;
        }

        AnimationAPI anim = weapon.getAnimation();
        if (weapon.getShip().isHulk())
        {
            anim.setFrame(0);
            return;
        }

        anim.setFrame(curFrame);
        timeSinceLastFrame += amount;
        if (timeSinceLastFrame >= TIME_BETWEEN_FRAMES)
        {
            timeSinceLastFrame = 0f;

            curFrame++;
            if (curFrame >= anim.getNumFrames())
            {
                curFrame = 1;
            }

            anim.setFrame(curFrame);
        }
    }
}