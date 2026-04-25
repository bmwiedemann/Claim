package net.yigitguven.claim.core;

import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager
{
    private static final Map<UUID, Selection> selections = new HashMap<>();

    public static Selection getOrCreateSelection(UUID uuid)
    {
        return selections.computeIfAbsent(uuid, k -> new Selection());
    }

    public static void clearSelection(UUID uuid)
    {
        selections.remove(uuid);
    }

    public static class Selection
    {
        public BlockPos pos1;
        public BlockPos pos2;

        public boolean isComplete()
        {
            return pos1 != null && pos2 != null;
        }

        public void reset()
        {
            pos1 = null;
            pos2 = null;
        }
    }
}
