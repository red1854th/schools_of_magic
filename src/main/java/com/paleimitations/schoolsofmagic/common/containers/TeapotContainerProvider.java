package com.paleimitations.schoolsofmagic.common.containers;

import com.paleimitations.schoolsofmagic.common.registries.ContainerRegistry;
import com.paleimitations.schoolsofmagic.common.tileentities.MortarTileEntity;
import com.paleimitations.schoolsofmagic.common.tileentities.TeapotTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.IContainerFactory;

public class TeapotContainerProvider extends AbstractContainerProvider<TeapotContainer> {

    private final TeapotTileEntity teapot;

    public TeapotContainerProvider(TeapotTileEntity teapot) {
        super(ContainerRegistry.TEAPOT_CONTAINER.get());
        this.teapot = teapot;
    }

    @Override
    public ITextComponent getDisplayName() {
        return teapot.getDisplayName();
    }

    @Override
    public TeapotContainer createMenu(int id, PlayerInventory plInventory, PlayerEntity player) {
        return (TeapotContainer) teapot.createMenu(id, plInventory, player);
    }

    public static TeapotContainer createFromPacket(int id, PlayerInventory plInventory, PacketBuffer data) {
        BlockPos pos = BlockPos.of(data.readLong());
        PlayerEntity player = plInventory.player;
        if(player.level.getBlockEntity(pos) instanceof TeapotTileEntity) {
            return (TeapotContainer) ((TeapotTileEntity) player.level.getBlockEntity(pos)).createMenu(id, plInventory, player);
        }
        return null;
    }

    @Override
    protected void writeExtraData(PacketBuffer buf) {
        buf.writeLong(teapot.getBlockPos().asLong());
    }

    public static class Factory implements IContainerFactory<TeapotContainer> {

        @Override
        public TeapotContainer create(int windowId, PlayerInventory inv, PacketBuffer data) {
            return TeapotContainerProvider.createFromPacket(windowId, inv, data);
        }
    }
}
