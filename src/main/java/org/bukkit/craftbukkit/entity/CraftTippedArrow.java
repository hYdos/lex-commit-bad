package org.bukkit.craftbukkit.entity;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TippedArrow;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.google.common.collect.ImmutableList;

import net.minecraft.server.EntityTippedArrow;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;

public class CraftTippedArrow extends CraftArrow implements TippedArrow {

    public CraftTippedArrow(CraftServer server, EntityTippedArrow entity) {
        super(server, entity);
    }

    @Override
    public EntityTippedArrow getHandle() {
        return (EntityTippedArrow) entity;
    }

    @Override
    public String toString() {
        return "CraftTippedArrow";
    }

    @Override
    public EntityType getType() {
        return EntityType.TIPPED_ARROW;
    }

    @Override
    public boolean addCustomEffect(PotionEffect effect, boolean override) {
        int effectId = effect.getType().getId();
        MobEffect existing = null;
        for (MobEffect mobEffect : getHandle().h) {
            if (MobEffectList.getId(mobEffect.getMobEffect()) == effectId) {
                existing = mobEffect;
            }
        }
        if (existing != null) {
            if (!override) {
                return false;
            }
            getHandle().h.remove(existing);
        }
        getHandle().a(CraftPotionUtil.fromBukkit(effect));
        getHandle().refreshEffects();
        return true;
    }

    @Override
    public void clearCustomEffects() {
        Validate.isTrue(getBasePotionData().getType() != PotionType.UNCRAFTABLE, "Tipped Arrows must have at least 1 effect");
        getHandle().h.clear();
        getHandle().refreshEffects();
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        for (MobEffect effect : getHandle().h) {
            builder.add(CraftPotionUtil.toBukkit(effect));
        }
        return builder.build();
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType type) {
        for (MobEffect effect : getHandle().h) {
            if (CraftPotionUtil.equals(effect.getMobEffect(), type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCustomEffects() {
        return !getHandle().h.isEmpty();
    }

    @Override
    public boolean removeCustomEffect(PotionEffectType effect) {
        int effectId = effect.getId();
        MobEffect existing = null;
        for (MobEffect mobEffect : getHandle().h) {
            if (MobEffectList.getId(mobEffect.getMobEffect()) == effectId) {
                existing = mobEffect;
            }
        }
        if (existing == null) {
            return false;
        }
        Validate.isTrue(getBasePotionData().getType() != PotionType.UNCRAFTABLE || getHandle().h.size() != 1, "Tipped Arrows must have at least 1 effect");
        getHandle().h.remove(existing);
        getHandle().refreshEffects();
        return true;
    }

    @Override
    public void setBasePotionData(PotionData data) {
        Validate.notNull(data, "PotionData cannot be null");
        Validate.isTrue(data.getType() != PotionType.UNCRAFTABLE || !getHandle().h.isEmpty(), "Tipped Arrows must have at least 1 effect");
        getHandle().setType(CraftPotionUtil.fromBukkit(data));
    }

    @Override
    public PotionData getBasePotionData() {
        return CraftPotionUtil.toBukkit(getHandle().getType());
    }
}
