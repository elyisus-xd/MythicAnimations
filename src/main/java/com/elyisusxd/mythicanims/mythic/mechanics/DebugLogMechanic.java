package com.elyisusxd.mythicanims.mythic.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.logging.Logger;

@MythicMechanic(author = "elyisusxd", name = "mmlog", aliases = {"debuglog"},
        description = "Log de depuracion que imprime en consola o actionbar")
public class DebugLogMechanic extends SkillMechanic implements INoTargetSkill {

    private final PlaceholderString message;
    private final String target;

    public DebugLogMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);
        this.message = PlaceholderString.of(
                mlc.getString(new String[]{"msg", "message", "m"}, "[mmlog] sin mensaje"));
        this.target = mlc.getString(new String[]{"target", "t"}, "console").toLowerCase();
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        String resolved = message.get(data);

        if ("actionbar".equals(target)) {
            AbstractEntity casterEntity = data.getCaster().getEntity();
            if (casterEntity != null && casterEntity.isPlayer()) {
                Player player = (Player) casterEntity.getBukkitEntity();
                player.sendActionBar(net.kyori.adventure.text.Component.text(resolved));
            }
        } else {
            Logger logger = Logger.getLogger("MythicAnimations");
            logger.info("[mmlog] " + resolved);
        }

        return SkillResult.SUCCESS;
    }
}
