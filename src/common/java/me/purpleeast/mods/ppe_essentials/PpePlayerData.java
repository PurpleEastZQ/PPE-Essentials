package me.purpleeast.mods.ppe_essentials;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PpePlayerData extends SavedData {
    private static final String NAME = PpeEssentials.MODID + "_player_data";

    private final Map<UUID, PpeLocation> homes = new HashMap<>();
    private final Map<UUID, PpeLocation> deathBacks = new HashMap<>();
    private final Map<UUID, PpeLocation> teleportBacks = new HashMap<>();
    private final Map<String, PpeLocation> warps = new HashMap<>();
    private final Set<UUID> tpaAuto = new HashSet<>();
    private final Set<UUID> fly = new HashSet<>();
    private final Set<UUID> god = new HashSet<>();
    private final Set<UUID> backNotice = new HashSet<>();
    private final Set<UUID> firstJoinNotice = new HashSet<>();

    public static PpePlayerData get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PpePlayerData::new, PpePlayerData::load, null),
                NAME
        );
    }

    private static PpePlayerData load(CompoundTag tag, HolderLookup.Provider provider) {
        PpePlayerData data = new PpePlayerData();
        data.readLocationMap(tag.getList("homes", Tag.TAG_COMPOUND), data.homes);
        data.readLocationMap(tag.getList("deathBacks", Tag.TAG_COMPOUND), data.deathBacks);
        data.readLocationMap(tag.getList("teleportBacks", Tag.TAG_COMPOUND), data.teleportBacks);
        data.readWarpMap(tag.getList("warps", Tag.TAG_COMPOUND), data.warps);

        data.readUuidSet(tag.getList("tpaAuto", Tag.TAG_COMPOUND), data.tpaAuto);
        data.readUuidSet(tag.getList("fly", Tag.TAG_COMPOUND), data.fly);
        data.readUuidSet(tag.getList("god", Tag.TAG_COMPOUND), data.god);
        data.readUuidSet(tag.getList("backNotice", Tag.TAG_COMPOUND), data.backNotice);
        data.readUuidSet(tag.getList("firstJoinNotice", Tag.TAG_COMPOUND), data.firstJoinNotice);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("homes", writeLocationMap(homes));
        tag.put("deathBacks", writeLocationMap(deathBacks));
        tag.put("teleportBacks", writeLocationMap(teleportBacks));
        tag.put("warps", writeWarpMap(warps));

        tag.put("tpaAuto", writeUuidSet(tpaAuto));
        tag.put("fly", writeUuidSet(fly));
        tag.put("god", writeUuidSet(god));
        tag.put("backNotice", writeUuidSet(backNotice));
        tag.put("firstJoinNotice", writeUuidSet(firstJoinNotice));
        return tag;
    }

    public Optional<PpeLocation> home(UUID player) {
        return Optional.ofNullable(homes.get(player));
    }

    public void setHome(UUID player, PpeLocation location) {
        homes.put(player, location);
        setDirty();
    }

    public boolean deleteHome(UUID player) {
        boolean removed = homes.remove(player) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public Optional<PpeLocation> deathBack(UUID player) {
        return Optional.ofNullable(deathBacks.get(player));
    }

    public void setDeathBack(UUID player, PpeLocation location) {
        deathBacks.put(player, location);
        setDirty();
    }

    public Optional<PpeLocation> teleportBack(UUID player) {
        return Optional.ofNullable(teleportBacks.get(player));
    }

    public void setTeleportBack(UUID player, PpeLocation location) {
        teleportBacks.put(player, location);
        setDirty();
    }

    public boolean isTpaAuto(UUID player) {
        return tpaAuto.contains(player);
    }

    public boolean toggleTpaAuto(UUID player) {
        return toggleUuidSet(tpaAuto, player);
    }

    public boolean isFlyEnabled(UUID player) {
        return fly.contains(player);
    }

    public boolean toggleFly(UUID player) {
        return toggleUuidSet(fly, player);
    }

    public boolean isGodEnabled(UUID player) {
        return god.contains(player);
    }

    public boolean toggleGod(UUID player) {
        return toggleUuidSet(god, player);
    }

    public boolean markBackNoticeShown(UUID player) {
        boolean added = backNotice.add(player);
        if (added) {
            setDirty();
        }
        return added;
    }

    public boolean hasFirstJoinNoticeShown(UUID player) {
        return firstJoinNotice.contains(player);
    }

    public boolean markFirstJoinNoticeShown(UUID player) {
        boolean added = firstJoinNotice.add(player);
        if (added) {
            setDirty();
        }
        return added;
    }

    public Optional<PpeLocation> warp(String name) {
        return Optional.ofNullable(warps.get(name));
    }

    public Set<String> warpNames() {
        return Set.copyOf(warps.keySet());
    }

    public void setWarp(String name, PpeLocation location) {
        warps.put(name, location);
        setDirty();
    }

    public boolean deleteWarp(String name) {
        boolean removed = warps.remove(name) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public void clearPlayerData() {
        homes.clear();
        deathBacks.clear();
        teleportBacks.clear();
        tpaAuto.clear();
        fly.clear();
        god.clear();
        clearNoticeData();
        setDirty();
    }

    public void clearNoticeData() {
        backNotice.clear();
        firstJoinNotice.clear();
        setDirty();
    }

    private void readLocationMap(ListTag list, Map<UUID, PpeLocation> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            target.put(entry.getUUID("uuid"), PpeLocation.load(entry.getCompound("location")));
        }
    }

    private ListTag writeLocationMap(Map<UUID, PpeLocation> source) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PpeLocation> item : source.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", item.getKey());
            entry.put("location", item.getValue().save());
            list.add(entry);
        }
        return list;
    }

    private void readWarpMap(ListTag list, Map<String, PpeLocation> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            target.put(entry.getString("name"), PpeLocation.load(entry.getCompound("location")));
        }
    }

    private ListTag writeWarpMap(Map<String, PpeLocation> source) {
        ListTag list = new ListTag();
        for (Map.Entry<String, PpeLocation> item : source.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("name", item.getKey());
            entry.put("location", item.getValue().save());
            list.add(entry);
        }
        return list;
    }

    private void readUuidSet(ListTag list, Set<UUID> target) {
        for (int i = 0; i < list.size(); i++) {
            target.add(list.getCompound(i).getUUID("uuid"));
        }
    }

    private ListTag writeUuidSet(Set<UUID> source) {
        ListTag list = new ListTag();
        for (UUID uuid : source) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            list.add(entry);
        }
        return list;
    }

    private boolean toggleUuidSet(Set<UUID> target, UUID uuid) {
        boolean enabled;
        if (target.contains(uuid)) {
            target.remove(uuid);
            enabled = false;
        } else {
            target.add(uuid);
            enabled = true;
        }
        setDirty();
        return enabled;
    }
}
