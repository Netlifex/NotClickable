package com.example.notclickable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class NotClickableData extends SavedData {
    private static final String DATA_NAME = "notclickable_data";
    private final Set<BlockPos> positions = new HashSet<>();

    public NotClickableData() {}

    public void add(BlockPos pos) {
        positions.add(pos);
        setDirty();
    }

    public void remove(BlockPos pos) {
        positions.remove(pos);
        setDirty();
    }

    public boolean contains(BlockPos pos) {
        return positions.contains(pos);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            listTag.add(posTag);
        }
        tag.put("positions", listTag);
        return tag;
    }

    public static NotClickableData load(CompoundTag tag) {
        NotClickableData data = new NotClickableData();
        ListTag listTag = tag.getList("positions", 10); // 10 – тип CompoundTag
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag posTag = listTag.getCompound(i);
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            int z = posTag.getInt("z");
            data.positions.add(new BlockPos(x, y, z));
        }
        return data;
    }

    public static NotClickableData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(NotClickableData::load, NotClickableData::new, DATA_NAME);
    }
    public Set<BlockPos> getPositions() {
        return positions;
    }



}
