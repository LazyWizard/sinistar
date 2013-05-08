package data.missions.sinistar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SinistarController implements EveryFrameCombatPlugin
{
    // How far away to hover in front of our current attack target
    private static final float OPTIMAL_DISTANCE_FROM_TARGET = 150f;
    // How many taunts are in sounds.json
    private static final int NUM_TAUNTS = 5;
    // The combat engine object used by this battle
    private CombatEngineAPI engine;
    // Sinistar's pre-battle fleet member object
    private FleetMemberAPI sinistarFleetMember;
    // Sinistar's in-battle ship object
    private ShipAPI sinistar;
    // Sinistar's manually calculated facing
    private float actualFacing = 0;
    // Whether one-time events (spawn, death) have been triggered yet
    private boolean hasSpawned = false, hasDied = false;
    // Time between taunts
    private IntervalUtil nextTaunt = new IntervalUtil(7.5f, 15f);

    public SinistarController(FleetMemberAPI sinistarFleetMember)
    {
        // This is Sinistar's FleetMemberAPI, created in MissionDefinition
        // We will use it in checkSpawn() to find Sinistar's ShipAPI object
        this.sinistarFleetMember = sinistarFleetMember;
    }

    private void checkSpawn()
    {
        // Find the ShipAPI that matches the FleetMemberAPI passed into the constructor
        sinistar = engine.getFleetManager(FleetSide.ENEMY).getShipFor(sinistarFleetMember);

        // For now, deliberately crash the game if Sinistar isn't found
        // Will replace with force-spawn later
        if (sinistar == null)
        {
            //sinistar = (ShipAPI) engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(
            //        "sinistar_Standard", new Vector2f(0,0), 0);
            throw new RuntimeException("Sinistar not found!");
        }

        // Give Sinistar some special properties
        MutableShipStatsAPI stats = sinistar.getMutableStats();
        stats.getSightRadiusMod().modifyMult("sinistar", 2.0f);
        stats.getEngineDamageTakenMult().modifyMult("sinistar", 0.001f);
        stats.getWeaponDamageTakenMult().modifyMult("sinistar", 0.001f);
        stats.getCombatEngineRepairTimeMult().modifyMult("sinistar", 9999f);
        stats.getCombatWeaponRepairTimeMult().modifyMult("sinistar", 9999f);

        // Play Sinistar's introductory taunt
        Global.getSoundPlayer().playUISound("sinistar_spawn", 1f, 1f);
        hasSpawned = true;
    }

    private void checkTaunt(float amount)
    {
        // If Sinistar is dead, perform his death scream
        if (sinistar.isHulk())
        {
            hasDied = true;
            Global.getSoundPlayer().playUISound("sinistar_death", 1f, 1f);
        }
        // If Sinistar can see the player, occasionally taunt them
        else if (engine.getFogOfWar(sinistar.getOwner()).isVisible(
                engine.getPlayerShip().getLocation()))
        {
            nextTaunt.advance(amount);
            if (nextTaunt.intervalElapsed())
            {
                Global.getSoundPlayer().playUISound("sinistar_taunt"
                        + (int) (Math.random() * NUM_TAUNTS), 1f, 1f);
            }
        }
    }

    private void doMovement(float amount)
    {
        // The location Sinistar will choose to travel towards
        Vector2f destination;

        // Prefer the AI-chosen target, use player ship if all else fails
        CombatEntityAPI target = sinistar.getShipTarget();
        if (target == null)
        {
            // Sinistar ALWAYS knows where you are :O
            target = engine.getPlayerShip();
        }

        if (target != null)
        {
            // Try to stay a certain distance away from the target
            destination = MathUtils.getPointOnCircumference(target.getLocation(),
                    target.getCollisionRadius() + sinistar.getCollisionRadius()
                    + OPTIMAL_DISTANCE_FROM_TARGET, MathUtils.getAngle(
                    target.getLocation(), sinistar.getLocation()));
        }
        else
        {
            // No targets available? Head to the center of the map and wait
            destination = new Vector2f(0, 0);
        }

        // Get mobility values from Sinistar's MutableShipStats
        MutableShipStatsAPI stats = sinistar.getMutableStats();
        float maxSpeed = stats.getMaxSpeed().getModifiedValue();
        float accel = stats.getAcceleration().getModifiedValue();
        float turnRate = stats.getMaxTurnRate().getModifiedValue();

        // Calculate and set actual facing and velocity manually
        float intendedFacing = MathUtils.getAngle(sinistar.getLocation(), destination);
        float facingChange = (intendedFacing - actualFacing) * amount * turnRate;
        float intendedSpeed = Math.min(sinistar.getVelocity().length()
                + (accel * amount), maxSpeed);
        actualFacing = MathUtils.clampAngle(actualFacing + facingChange);
        sinistar.getVelocity().set(MathUtils.getPointOnCircumference(null,
                intendedSpeed, actualFacing));
    }

    @Override
    public void advance(float amount, List events)
    {
        // Always face the same direction, old-school style
        // This means we will need to handle movement ourselves
        if (sinistar != null)
        {
            sinistar.setFacing(90);
        }

        // If the game is paused or Sinistar is dead, don't do anything
        if (engine.isPaused() || hasDied)
        {
            return;
        }

        // One time setup at battle start
        if (!hasSpawned)
        {
            checkSpawn();
        }

        // Call methods that control Sinistar's behavior
        checkTaunt(amount);
        doMovement(amount);
    }

    @Override
    public void init(CombatEngineAPI engine)
    {
        this.engine = engine;
    }
}