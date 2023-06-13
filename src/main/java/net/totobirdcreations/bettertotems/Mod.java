package net.totobirdcreations.bettertotems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;


public class Mod implements ModInitializer {

    public static final GameRules.Key<GameRules.BooleanRule> TOTEMS_IN_VOID      = GameRuleRegistry.register("totemsInVoid"      , GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> TOTEMS_IN_INVENTORY = GameRuleRegistry.register("totemsInInventory" , GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));

    @Override
    public void onInitialize() {}

}
