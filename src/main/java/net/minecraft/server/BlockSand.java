package net.minecraft.server;

import java.util.Random;

public class BlockSand extends Block {

    public static boolean instaFall = false;

    public BlockSand(int i, int j) {
        super(i, j, Material.SAND);
        this.a(CreativeModeTab.b);
    }

    public BlockSand(int i, int j, Material material) {
        super(i, j, material);
    }

    public void onPlace(World world, int i, int j, int k) {
        world.a(i, j, k, this.id, this.r_());
    }

    public void doPhysics(World world, int i, int j, int k, int l) {
        world.a(i, j, k, this.id, this.r_());
    }

    public void b(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            this.l(world, i, j, k);
        }
    }

    private void l(World world, int i, int j, int k) {
        if (canFall(world, i, j - 1, k) && j >= 0) {
            byte b0 = 32;

            if (!instaFall && world.d(i - b0, j - b0, k - b0, i + b0, j + b0, k + b0)) {
                if (!world.isStatic) {
                    EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, (double) ((float) i + 0.5F), (double) ((float) j + 0.5F), (double) ((float) k + 0.5F), this.id, world.getData(i, j, k));

                    this.a(entityfallingblock);
                    world.addEntity(entityfallingblock);
                }
            } else {
                world.setTypeId(i, j, k, 0);

                while (canFall(world, i, j - 1, k) && j > 0) {
                    --j;
                }

                if (j > 0) {
                    world.setTypeId(i, j, k, this.id);
                }
            }
        }
    }

    protected void a(EntityFallingBlock entityfallingblock) {}

    public int r_() {
        return 5;
    }

    public static boolean canFall(World world, int i, int j, int k) {
        int l = world.getTypeId(i, j, k);

        if (l == 0) {
            return true;
        } else if (l == Block.FIRE.id) {
            return true;
        } else {
            Material material = Block.byId[l].material;

            return material == Material.WATER ? true : material == Material.LAVA;
        }
    }

    public void a_(World world, int i, int j, int k, int l) {}
}
