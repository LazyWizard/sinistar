package data.missions.sinistar;

import com.fs.starfarer.api.campaign.CargoAPI.CrewXPLevel;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

// This is all temporary, will rewrite once the mod is further along
public class MissionDefinition implements MissionDefinitionPlugin
{
    @Override
    public void defineMission(MissionDefinitionAPI api)
    {
        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false, 10);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 10);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Player");
        api.setFleetTagline(FleetSide.ENEMY, "Sinistar");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat Sinistar and his forces!");

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "apogee_Balanced", FleetMemberType.SHIP, "Last Hope", true, CrewXPLevel.ELITE);
        //api.defeatOnShipLoss("Last Hope");
        api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "sunder_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "wolf_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "wolf_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "wolf_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
        api.addToFleet(FleetSide.PLAYER, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
        api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
        api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
        api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);

        // Set up the enemy fleet and add the Sinistar controller
        FleetMemberAPI sinistar = api.addToFleet(FleetSide.ENEMY, "sinistar_Standard",
                FleetMemberType.SHIP, "", true, CrewXPLevel.ELITE);
        sinistar.getCaptain().setPersonality("fearless");
        api.addPlugin(new SinistarController(sinistar));

        // Set up the map.
        float width = 5000f;
        float height = 9000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);
    }
}
