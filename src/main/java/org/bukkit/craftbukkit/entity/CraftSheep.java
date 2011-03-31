package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntitySheep;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Sheep;

public class CraftSheep extends CraftAnimals implements Sheep {
    public CraftSheep(CraftServer server, EntitySheep entity) {
        super(server, entity);
    }

    @Override
    public EntitySheep getHandle() {
        return (EntitySheep) entity;
    }

    @Override
    public String toString() {
        return "CraftSheep";
    }

    public DyeColor getColor() {
        return DyeColor.getByData((byte) getHandle().o());
    }

    public void setColor(DyeColor color) {
        getHandle().a_(color.getData());
    }

    public boolean isSheared() {
        return getHandle().j_();
    }

    public void setSheared(boolean flag) {
        getHandle().a(flag);
    }

}
