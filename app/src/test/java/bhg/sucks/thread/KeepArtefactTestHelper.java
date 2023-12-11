package bhg.sucks.thread;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.List;

import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

public class KeepArtefactTestHelper {

    List<KeepRule> mainHallRules() {
        return List.of(
                KeepRule.builder()
                        .name("Mortar/Artillery/Transporter")
                        .category(Category.Weapon)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.Weapon, List.of(
                                Skill.ParatroopersFromTransportsDamage,
                                Skill.ParatroopersFromTransportsHitpoints,
                                Skill.RangedSiegeDamage,
                                Skill.RangedSiegeHitpoints,
                                Skill.MortarTroopDamage,
                                Skill.MortarTroopHitpoints)))
                        .build(),
                KeepRule.builder()
                        .name("Commando/Fighter")
                        .category(Category.Weapon)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.Weapon, List.of(
                                Skill.GuerrillaHitpoints,
                                Skill.FighterDamage,
                                Skill.FighterHitpoints)))
                        .build(),
                KeepRule.builder()
                        .name("Anti-Everything")
                        .category(Category.Armor)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.Armor, List.of(
                                Skill.InvadingFighterDamage,
                                Skill.InvadingFighterHitpoints,
                                Skill.InvadingHeavyTankDamage,
                                Skill.InvadingHeavyTankHitpoints,
                                Skill.DefenderHitpoints,
                                Skill.DefenderDamage)))
                        .build(),
                KeepRule.builder()
                        .name("Loot")
                        .category(Category.Jewelry)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(ImmutableMap.of(Category.Jewelry, List.of(
                                Skill.AllResourceLooted,
                                Skill.AllResourceLooted)))
                        .optionalSkills(ImmutableMap.of(Category.Jewelry, List.of(
                                Skill.OilLooted,
                                Skill.EnemyAirDefenseDamage,
                                Skill.EnemyAirDefenseHitpoints)))
                        .build(),
                KeepRule.builder()
                        .name("Def+Oil")
                        .category(Category.Pottery)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.Pottery, List.of(
                                Skill.AirDefenseDamage,
                                Skill.AirDefenseHitpoints,
                                Skill.TowerHitpoints,
                                Skill.TowerDamage,
                                Skill.OilFromOilWells)))
                        .build()
        );
    }

    List<KeepRule> warHallMortarRules() {
        return List.of(
                KeepRule.builder()
                        .name("Mortar/Artillery/Transporter")
                        .category(Category.WarWeapon)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.WarWeapon, List.of(
                                Skill.WarParatroopersFromTransportsDamage,
                                Skill.WarRangedSiegeDamage,
                                Skill.WarMortarTroopDamage)))
                        .build(),
                KeepRule.builder()
                        .name("Mortar/Artillery/Transporter")
                        .category(Category.WarArmor)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.WarArmor, List.of(
                                Skill.WarParatroopersFromTransportsHitpoints,
                                Skill.WarRangedSiegeHitpoints,
                                Skill.WarMortarTroopHitpoints)))
                        .build(),
                KeepRule.builder()
                        .name("Offense!")
                        .category(Category.WarEquipment)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(Maps.newHashMap())
                        .optionalSkills(ImmutableMap.of(Category.WarEquipment, List.of(
                                Skill.WarEnemyDefenderSpawnTime,
                                Skill.WarAllEnemyDefensiveTowersDamage,
                                Skill.WarAllEnemyDefensiveTowersHitpoints)))
                        .build()
        );
    }
}
