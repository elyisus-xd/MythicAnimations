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
import me.feeldev.animorph.api.AnimorphProvider;
import me.feeldev.animorph.api.IMorphAPI;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;

@MythicMechanic(author = "elyisusxd", name = "morphemote", aliases = {"morphanim"},
        description = "Reproduce un emote de Animorph en el caster (requiere Animorph disponible)")
public class MorphEmoteMechanic extends SkillMechanic implements INoTargetSkill {

    private final PlaceholderString emote;
    private final PlaceholderString layer;

    public MorphEmoteMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);
        this.emote = PlaceholderString.of(
                mlc.getString(new String[]{"emote", "e"}, ""));
        this.layer = PlaceholderString.of(
                mlc.getString(new String[]{"layer", "l"}, ""));
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        // Verifica que Animorph este disponible (softdepend)
        if (!AnimorphProvider.isAvailable()) {
            return SkillResult.CONDITION_FAILED;
        }

        // El caster debe ser un Player
        AbstractEntity casterEntity = data.getCaster().getEntity();
        if (casterEntity == null || !casterEntity.isPlayer()) {
            return SkillResult.INVALID_TARGET;
        }
        Player player = (Player) casterEntity.getBukkitEntity();

        // Resuelve el emote
        String emoteStr = emote.get(data);
        if (emoteStr.isEmpty()) {
            return SkillResult.INVALID_CONFIG;
        }

        // Obtiene la API y reproduce el emote
        IMorphAPI<Player> api = AnimorphProvider.getApi();

        String layerStr = layer.get(data);
        if (layerStr.isEmpty()) {
            api.playEmote(player, emoteStr, null);
        } else {
            api.playEmote(player, emoteStr, Set.of(layerStr));
        }

        return SkillResult.SUCCESS;
    }
}
