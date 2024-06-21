package fr.frinn.custommachinery.common.util;

import net.minecraft.world.entity.player.Player;

public class ExperienceUtils {

    public static int getXpNeededForNextLevel(int currentLevel) {
        if (currentLevel >= 30) {
            return 112 + (currentLevel - 30) * 9;
        }
        if (currentLevel >= 15) {
            return 37 + (currentLevel - 15) * 5;
        }
        return 7 + currentLevel * 2;
    }

    public static int getXpFromLevel(int level) {
        if (level >= 32)
            return (int) ((4.5 * Math.pow(level, 2)) - 162.5 * level + 2220);
        else if (level >= 17)
            return (int) ((2.5 * Math.pow(level, 2)) - 40.5 * level + 360);
        else
            return (int) (Math.pow(level, 2) + 6L * level);
    }

    public static int getLevelFromXp(long experience) {
        if (experience >= 1508) {
            return (int) ((325.0 / 18) + (Math.sqrt(( 2.0 /9) * (experience - (54215.0 / 72)))));
        } else if (experience >= 353) {
            return (int) ((81.0 / 10) + (Math.sqrt(( 2.0 /5) * (experience - (7839.0 / 40)))));
        } else {
            return (int) (Math.sqrt(experience + 9) - 3);
        }
    }

    public static int getPlayerTotalXp(Player player) {
        return getXpFromLevel(player.experienceLevel) + (int) Math.floor(player.experienceProgress * getXpNeededForNextLevel(player.experienceLevel));
    }
}
